import { Component, inject, OnInit, signal } from '@angular/core';
import {
  NgbActiveModal,
  NgbDropdown,
  NgbDropdownItem,
  NgbDropdownMenu,
  NgbDropdownToggle,
} from '@ng-bootstrap/ng-bootstrap';
import { FormsModule } from '@angular/forms';
import { ModalHeaderComponent } from '../../../shared/modal-header/modal-header.component';
import { ButtonComponent } from '../../../shared/button/button.component';
import { ResourceService } from '../../services/resource.service';
import { CopyFromCandidate } from '../../models/copy-from-candidate';
import { ToastService } from '../../../shared/elements/toast/toast.service';
import { LoadingIndicatorComponent } from '../../../shared/elements/loading-indicator.component';
import { IconComponent } from '../../../shared/icon/icon.component';

@Component({
  selector: 'app-copy-from-resource-dialog',
  standalone: true,
  imports: [
    FormsModule,
    ModalHeaderComponent,
    ButtonComponent,
    LoadingIndicatorComponent,
    IconComponent,
    NgbDropdown,
    NgbDropdownMenu,
    NgbDropdownToggle,
    NgbDropdownItem,
  ],
  templateUrl: './copy-from-resource-dialog.component.html',
})
export class CopyFromResourceDialogComponent implements OnInit {
  activeModal = inject(NgbActiveModal);
  private resourceService = inject(ResourceService);
  private toastService = inject(ToastService);

  resourceId: number;
  resourceTypeName: string;

  candidates = signal<CopyFromCandidate[]>([]);
  selectedReleaseResourceIds = signal<{ [groupId: number]: number }>({});
  isLoading = signal(false);
  isCopying = signal(false);

  ngOnInit() {
    this.loadCandidates();
  }

  loadCandidates() {
    this.isLoading.set(true);
    this.resourceService.getCopyFromCandidates(this.resourceId).subscribe({
      next: (candidates) => {
        this.candidates.set(candidates);
        const initialSelections: { [groupId: number]: number } = {};
        for (const candidate of candidates) {
          if (candidate.releases.length > 0) {
            initialSelections[candidate.groupId] = candidate.releases[0].resourceId;
          }
        }
        this.selectedReleaseResourceIds.set(initialSelections);
        this.isLoading.set(false);
      },
      error: (error) => {
        console.error('Failed to load copy-from candidates:', error);
        this.toastService.error('Failed to load copy-from candidates.');
        this.isLoading.set(false);
        this.activeModal.dismiss('error');
      },
    });
  }

  onReleaseChange(groupId: number, resourceId: number) {
    this.selectedReleaseResourceIds.update((selections) => ({
      ...selections,
      [groupId]: resourceId,
    }));
  }

  getSelectedReleaseName(groupId: number): string {
    const selectedResourceId = this.selectedReleaseResourceIds()[groupId];
    if (!selectedResourceId) {
      return '';
    }

    const candidate = this.candidates().find((c) => c.groupId === groupId);
    if (!candidate) {
      return '';
    }

    const release = candidate.releases.find((r) => r.resourceId === selectedResourceId);
    return release?.releaseName || '';
  }

  copyFrom(groupId: number) {
    const originResourceId = this.selectedReleaseResourceIds()[groupId];
    if (!originResourceId) {
      return;
    }
    this.isCopying.set(true);
    this.resourceService.copyFromResource(this.resourceId, originResourceId).subscribe({
      next: () => {
        this.toastService.success('Copy successful');
        this.isCopying.set(false);
        this.activeModal.close('copied');
      },
      error: (error) => {
        console.error('Copy from resource failed:', error);
        this.toastService.error('Copy from resource failed.');
        this.isCopying.set(false);
      },
    });
  }
}
