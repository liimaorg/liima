import { Component, computed, inject, input, signal, ViewChild, TemplateRef } from '@angular/core';
import { LoadingIndicatorComponent } from '../../../shared/elements/loading-indicator.component';
import { AuthService } from '../../../auth/auth.service';
import { TileComponent } from '../../../shared/tile/tile.component';
import { Release } from '../../models/release';
import { Router, ActivatedRoute } from '@angular/router';
import { ButtonComponent } from '../../../shared/button/button.component';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { ResourceService } from '../../services/resource.service';
import { Resource } from '../../models/resource';
import { FormsModule } from '@angular/forms';
import { ModalHeaderComponent } from '../../../shared/modal-header/modal-header.component';
import { ToastService } from '../../../shared/elements/toast/toast.service';
import { TableComponent, TableColumnType, EntryAction, EntryActionOutput } from '../../../shared/table/table.component';

@Component({
  selector: 'app-resource-releases',
  standalone: true,
  imports: [
    LoadingIndicatorComponent,
    TileComponent,
    ButtonComponent,
    FormsModule,
    ModalHeaderComponent,
    TableComponent,
  ],
  templateUrl: './resource-releases.component.html',
})
export class ResourceReleasesComponent {
  private authService = inject(AuthService);
  private modalService = inject(NgbModal);
  private resourceService = inject(ResourceService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);
  private toastService = inject(ToastService);

  isLoading = signal(false);
  releaseIdToDelete = signal<number | null>(null);
  availableReleases = signal<Release[]>([]);
  selectedReleaseId = signal<number | null>(null);
  isCreatingRelease = signal(false);
  releaseIdToChange = signal<number | null>(null);
  isChangingRelease = signal(false);

  @ViewChild('createReleaseModal') createReleaseModal!: TemplateRef<void>;
  @ViewChild('changeReleaseModal') changeReleaseModal!: TemplateRef<void>;
  @ViewChild('deleteReleaseConfirmation') deleteConfirmationTemplate!: TemplateRef<void>;

  id = input.required<number>();
  releases = input.required<Release[]>();
  contextId = input.required<number>();
  resource = input.required<Resource>();

  tableHeaders: TableColumnType<Release>[] = [{ key: 'name', columnTitle: 'Release' }];

  rowStyle = computed(() => {
    const currentId = this.id();
    return (row: Release) => (row.id === currentId ? 'font-weight: bold' : '');
  });

  releaseToDeleteName = computed(() => this.releases().find((r) => r.id === this.releaseIdToDelete())?.name ?? '');
  releaseToChangeName = computed(() => this.releases().find((r) => r.id === this.releaseIdToChange())?.name ?? '');

  onRowClick(row: Release) {
    void this.router.navigate(['/resource/edit'], {
      queryParams: { ctx: this.contextId(), id: row.id, selectedResourceTypeId: this.resource()?.resourceTypeId },
    });
  }

  onTableAction(event: EntryActionOutput) {
    if (event.action === EntryAction.navigate) {
      void this.router.navigate(['/resource/edit'], {
        queryParams: { ctx: this.contextId(), id: event.id, selectedResourceTypeId: this.resource()?.resourceTypeId },
      });
    } else if (event.action === EntryAction.edit) {
      this.showChangeReleaseModal(event.id);
    } else if (event.action === EntryAction.delete) {
      this.showDeleteConfirmation(this.deleteConfirmationTemplate, event.id);
    }
  }

  permissions = computed(() => {
    if (this.authService.restrictions().length > 0) {
      const resourceTypeName = this.resource()?.type ?? null;
      const resourceGroupId = this.resource()?.resourceGroupId ?? null;
      return {
        canAddRelease: this.authService.hasPermission('RESOURCE', 'CREATE', resourceTypeName, resourceGroupId),
        canChangeRelease: this.authService.hasPermission('RELEASE', 'UPDATE', resourceTypeName, resourceGroupId),
        canDeleteRelease: this.authService.hasPermission('RESOURCE', 'DELETE', resourceTypeName, resourceGroupId),
      };
    }
    return { canAddRelease: false, canChangeRelease: false, canDeleteRelease: false };
  });

  addRelease() {
    this.isLoading.set(true);
    this.resourceService.getAvailableReleasesForResource(this.id()).subscribe({
      next: (releases) => {
        this.availableReleases.set(releases);
        this.selectedReleaseId.set(null);
        this.isLoading.set(false);
        this.showCreateReleaseModal();
      },
      error: (error) => {
        console.error('Failed to load available releases:', error);
        this.toastService.error('Failed to load available releases.');
        this.isLoading.set(false);
      },
    });
  }

  showCreateReleaseModal() {
    const modalRef = this.modalService.open(this.createReleaseModal);
    modalRef.result.then(
      () => {
        if (this.selectedReleaseId()) {
          this.createRelease(this.selectedReleaseId()!);
        }
      },
      () => {
        this.selectedReleaseId.set(null);
      },
    );
  }

  createRelease(releaseId: number) {
    const selectedRelease = this.availableReleases().find((r) => r.id === releaseId);
    if (!selectedRelease || !selectedRelease.name) {
      console.error('Selected release not found');
      return;
    }

    const currentResource = this.resource();
    if (!currentResource || !currentResource.name) {
      console.error('Current resource not found');
      return;
    }

    const currentReleaseName = this.releases().find((r) => r.id === this.id())?.name;
    if (!currentReleaseName) {
      console.error('Current release name not found');
      return;
    }

    this.isCreatingRelease.set(true);
    this.resourceService
      .createResourceRelease(currentResource.name, selectedRelease.name, currentReleaseName)
      .subscribe({
        next: () => {
          this.isCreatingRelease.set(false);
          this.selectedReleaseId.set(null);
          this.resourceService.setIdForResource(this.id());
        },
        error: (error) => {
          console.error('Failed to create release:', error);
          this.toastService.error('Failed to create release.');
          this.isCreatingRelease.set(false);
          this.selectedReleaseId.set(null);
        },
      });
  }

  showChangeReleaseModal(releaseId: number) {
    this.releaseIdToChange.set(releaseId);
    this.isLoading.set(true);
    this.resourceService.getAvailableReleasesForResource(releaseId).subscribe({
      next: (releases) => {
        this.availableReleases.set(releases);
        this.selectedReleaseId.set(null);
        this.isLoading.set(false);
        this.openChangeReleaseModal();
      },
      error: (error) => {
        console.error('Failed to load available releases:', error);
        this.toastService.error('Failed to load available releases.');
        this.isLoading.set(false);
        this.releaseIdToChange.set(null);
      },
    });
  }

  openChangeReleaseModal() {
    const modalRef = this.modalService.open(this.changeReleaseModal);
    modalRef.result.then(
      () => {
        if (this.selectedReleaseId() && this.releaseIdToChange()) {
          this.changeRelease(this.releaseIdToChange()!, this.selectedReleaseId()!);
        }
      },
      () => {
        this.selectedReleaseId.set(null);
        this.releaseIdToChange.set(null);
      },
    );
  }

  changeRelease(resourceId: number, releaseId: number) {
    this.isChangingRelease.set(true);
    this.resourceService.changeResourceRelease(resourceId, releaseId).subscribe({
      next: () => {
        this.isChangingRelease.set(false);
        this.selectedReleaseId.set(null);
        this.releaseIdToChange.set(null);
        // Reload the resource and releases data to reflect the change
        this.resourceService.setIdForResource(resourceId);
        // Navigate to the changed resource (stays on the same resource, now in new release)
        void this.router.navigate([], {
          relativeTo: this.route,
          queryParams: { id: resourceId },
          queryParamsHandling: 'merge',
        });
      },
      error: (error) => {
        console.error('Failed to change release:', error);
        this.toastService.error('Failed to change release.');
        this.isChangingRelease.set(false);
        this.selectedReleaseId.set(null);
        this.releaseIdToChange.set(null);
      },
    });
  }

  showDeleteConfirmation(content: unknown, releaseId: number) {
    this.releaseIdToDelete.set(releaseId);
    this.modalService.open(content).result.then(
      () => {
        this.deleteRelease(releaseId);
      },
      () => {
        this.releaseIdToDelete.set(null);
      },
    );
  }

  deleteRelease(resourceIdToDelete: number) {
    // Note: release.id is actually the Resource ID, not the Release ID
    // This is how the backend constructs the ReleaseDTO
    this.isLoading.set(true);
    this.resourceService.deleteResourceByResourceId(resourceIdToDelete).subscribe({
      next: () => {
        this.isLoading.set(false);
        this.releaseIdToDelete.set(null);
        // Only navigate if we deleted the currently selected release
        if (resourceIdToDelete === this.id()) {
          const remainingReleases = this.releases().filter((r) => r.id !== resourceIdToDelete);
          if (remainingReleases.length > 0) {
            // Navigate to the first remaining release
            void this.router.navigate([], {
              relativeTo: this.route,
              queryParams: { id: remainingReleases[0].id },
              queryParamsHandling: 'merge',
            });
          } else {
            // No releases left, navigate to resources list
            void this.router.navigate(['/resources']);
          }
        } else {
          // Reload the releases list to remove the deleted release from the UI
          this.resourceService.setIdForResource(this.id());
        }
      },
      error: (error) => {
        console.error('Failed to delete release:', error);
        this.toastService.error('Failed to delete release.');
        this.isLoading.set(false);
        this.releaseIdToDelete.set(null);
      },
    });
  }
}
