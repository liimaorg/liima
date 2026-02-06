import { Component, computed, inject, input, signal } from '@angular/core';
import { LoadingIndicatorComponent } from '../../../shared/elements/loading-indicator.component';
import { AuthService } from '../../../auth/auth.service';
import { TileComponent } from '../../../shared/tile/tile.component';
import { Release } from '../../models/release';
import { RouterLink } from '@angular/router';
import { IconComponent } from '../../../shared/icon/icon.component';
import { ButtonComponent } from '../../../shared/button/button.component';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { ModalHeaderComponent } from '../../../shared/modal-header/modal-header.component';

@Component({
  selector: 'app-resource-releases',
  standalone: true,
  imports: [LoadingIndicatorComponent, TileComponent, RouterLink, IconComponent, ButtonComponent, ModalHeaderComponent],
  templateUrl: './resource-releases.component.html',
})
export class ResourceReleasesComponent {
  private authService = inject(AuthService);
  private modalService = inject(NgbModal);

  isLoading = signal(false);
  selectedRelease = signal<Release | null>(null);

  id = input.required<number>();
  releases = input.required<Release[]>();
  contextId = input.required<number>();
  resourceTypeId = input.required<number>();

  // same permissions for crud
  permissions = computed(() => {
    if (this.authService.restrictions().length > 0) {
      return {
        //canAddRelease: this.authService.hasPermission('RESOURCE', 'UPDATE', null, null, this.context().name),
        canAddRelease: true, // TODO: replace with actual permission check
      };
    }
    return { canAddRelease: false };
  });

  addRelease() {
    console.log('add release');
  }

  showDeleteConfirmation(content: unknown, release: Release) {
    this.selectedRelease.set(release);
    this.modalService.open(content).result.then(
      () => {
        this.deleteRelease(release.id);
      },
      () => {
        this.selectedRelease.set(null);
      },
    );
  }

  deleteRelease(releaseId: number) {
    console.log('delete release', releaseId);
    this.selectedRelease.set(null);
  }
}
