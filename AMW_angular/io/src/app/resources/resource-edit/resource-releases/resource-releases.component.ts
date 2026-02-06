import { Component, computed, inject, input, signal, ViewChild, TemplateRef } from '@angular/core';
import { LoadingIndicatorComponent } from '../../../shared/elements/loading-indicator.component';
import { AuthService } from '../../../auth/auth.service';
import { TileComponent } from '../../../shared/tile/tile.component';
import { Release } from '../../models/release';
import { RouterLink, Router, ActivatedRoute } from '@angular/router';
import { IconComponent } from '../../../shared/icon/icon.component';
import { ButtonComponent } from '../../../shared/button/button.component';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { ResourceService } from '../../services/resource.service';
import { Resource } from '../../models/resource';
import { FormsModule } from '@angular/forms';
import { ModalHeaderComponent } from '../../../shared/modal-header/modal-header.component';

@Component({
  selector: 'app-resource-releases',
  standalone: true,
  imports: [
    LoadingIndicatorComponent,
    TileComponent,
    RouterLink,
    IconComponent,
    ButtonComponent,
    FormsModule,
    ModalHeaderComponent,
  ],
  templateUrl: './resource-releases.component.html',
})
export class ResourceReleasesComponent {
  private authService = inject(AuthService);
  private modalService = inject(NgbModal);
  private resourceService = inject(ResourceService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);

  isLoading = signal(false);
  selectedRelease = signal<Release | null>(null);
  availableReleases = signal<Release[]>([]);
  selectedReleaseId: number | null = null;
  isCreatingRelease = signal(false);

  @ViewChild('createReleaseModal') createReleaseModal!: TemplateRef<any>;

  id = input.required<number>();
  releases = input.required<Release[]>();
  contextId = input.required<number>();
  resource = input.required<Resource>();

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
    this.isLoading.set(true);
    this.resourceService.getAvailableReleasesForResource(this.id()).subscribe({
      next: (releases) => {
        this.availableReleases.set(releases);
        this.selectedReleaseId = null;
        this.isLoading.set(false);
        this.showCreateReleaseModal();
      },
      error: (error) => {
        console.error('Failed to load available releases:', error);
        this.isLoading.set(false);
      },
    });
  }

  showCreateReleaseModal() {
    const modalRef = this.modalService.open(this.createReleaseModal);
    modalRef.result.then(
      () => {
        if (this.selectedReleaseId) {
          this.createRelease(this.selectedReleaseId);
        }
      },
      () => {
        this.selectedReleaseId = null;
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
          this.selectedReleaseId = null;
          this.resourceService.setIdForResource(this.id());
        },
        error: (error) => {
          console.error('Failed to create release:', error);
          this.isCreatingRelease.set(false);
          this.selectedReleaseId = null;
        },
      });
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

  deleteRelease(resourceIdToDelete: number) {
    // Note: release.id is actually the Resource ID, not the Release ID
    // This is how the backend constructs the ReleaseDTO
    this.isLoading.set(true);
    this.resourceService.deleteResourceByResourceId(resourceIdToDelete).subscribe({
      next: () => {
        this.isLoading.set(false);
        this.selectedRelease.set(null);
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
        this.isLoading.set(false);
        this.selectedRelease.set(null);
      },
    });
  }
}
