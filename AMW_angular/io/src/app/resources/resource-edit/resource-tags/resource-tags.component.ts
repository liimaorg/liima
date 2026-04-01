import { Component, computed, effect, inject, input, signal } from '@angular/core';
import { LoadingIndicatorComponent } from '../../../shared/elements/loading-indicator.component';
import { TileComponent } from '../../../shared/tile/tile.component';
import { AuthService } from '../../../auth/auth.service';
import { Resource } from '../../models/resource';
import { ResourceTagsService } from '../../services/resource-tags.service';
import { ResourceTag } from '../../models/resource-tag';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { TagData, TagEditModalComponent } from '../tag-edit-modal/tag-edit-modal.component';
import { ToastService } from '../../../shared/elements/toast/toast.service';
import { DatePipe } from '@angular/common';

@Component({
  selector: 'app-resource-tags',
  templateUrl: './resource-tags.component.html',
  standalone: true,
  imports: [LoadingIndicatorComponent, TileComponent, DatePipe],
})
export class ResourceTagsComponent {
  private authService = inject(AuthService);

  resource = input.required<Resource>();

  private resourceTagsService = inject(ResourceTagsService);
  private modalService = inject(NgbModal);
  private toastService = inject(ToastService);

  protected readonly isLoading = signal<boolean>(false);
  protected readonly tags = signal<ResourceTag[]>([]);
  protected readonly isApplicationServer = computed(() => this.resource()?.type === 'APPLICATIONSERVER');

  protected readonly permissions = computed(() => {
    if (this.authService.restrictions().length > 0) {
      const resourceTypeName = this.resource()?.type ?? null;
      const resourceGroupId = this.resource()?.resourceGroupId ?? null;
      return {
        canTagCurrentState: this.authService.hasPermission('RESOURCE', 'UPDATE', resourceTypeName, resourceGroupId),
      };
    } else {
      return { canTagCurrentState: false };
    }
  });

  constructor() {
    effect(() => {
      const resource = this.resource();
      if (resource?.id) {
        this.loadTags();
      }
    });
  }

  private loadTags() {
    const resourceId = this.resource()?.id;
    if (!resourceId) return;

    this.isLoading.set(true);
    this.resourceTagsService.getResourceTags(resourceId).subscribe({
      next: (tags) => {
        this.tags.set(tags);
        this.isLoading.set(false);
      },
      error: (error) => {
        console.error('Failed to load tags:', error);
        this.isLoading.set(false);
        this.toastService.error('Failed to load tags.');
      },
    });
  }

  protected tagCurrentState() {
    const modalRef = this.modalService.open(TagEditModalComponent);
    modalRef.componentInstance.resource = this.resource();
    modalRef.componentInstance.saveTag.subscribe((tagData: TagData) => this.createTag(tagData));
  }

  private createTag(tagData: TagData) {
    const resourceId = this.resource()?.id;
    if (!resourceId) return;

    this.resourceTagsService
      .createTag(resourceId, {
        label: tagData.label,
        tagDate: tagData.tagDate,
      })
      .subscribe({
        next: () => {
          this.toastService.success(`New tag '${tagData.label}' created.`);
          this.loadTags();
        },
        error: (error) => {
          console.error('Failed to create tag:', error);
          const errorMessage = error?.error?.message || 'Failed to create tag.';
          this.toastService.error(errorMessage);
        },
      });
  }
}
