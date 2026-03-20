import { inject, Injectable, signal } from '@angular/core';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { PropertyDescriptorService } from './property-descriptor.service';
import { ToastService } from '../../shared/elements/toast/toast.service';

export interface DeleteDescriptor {
  id: number;
  name: string;
  errorMessage?: string;
}

@Injectable()
export class PropertyDeleteModalService {
  private modalService = inject(NgbModal);
  private descriptorService = inject(PropertyDescriptorService);
  private toastService = inject(ToastService);

  descriptorToDelete = signal<DeleteDescriptor | null>(null);
  isDeleting = signal<boolean>(false);

  showDeleteConfirmation(content: unknown, descriptor: DeleteDescriptor) {
    this.descriptorToDelete.set(descriptor);
    this.isDeleting.set(false);

    this.modalService.open(content).result.then(
      () => {
        // Modal dismissed/closed
        this.descriptorToDelete.set(null);
        this.isDeleting.set(false);
      },
      () => {
        // Modal dismissed
        this.descriptorToDelete.set(null);
        this.isDeleting.set(false);
      },
    );
  }

  confirmDelete(
    modal: any,
    resourceId: number | undefined,
    resourceTypeId: number | undefined,
    onSuccess: () => void,
    destroy$: Subject<void>,
  ) {
    const descriptor = this.descriptorToDelete();
    if (!descriptor) return;

    this.isDeleting.set(true);

    if (descriptor.errorMessage) {
      // Force delete
      this.performForceDelete(descriptor.id, resourceId, resourceTypeId, modal, onSuccess, destroy$);
    } else {
      // Normal delete
      this.performDelete(descriptor.id, resourceId, resourceTypeId, modal, onSuccess, destroy$);
    }
  }

  private performDelete(
    id: number,
    resourceId: number | undefined,
    resourceTypeId: number | undefined,
    modal: any,
    onSuccess: () => void,
    destroy$: Subject<void>,
  ) {
    this.descriptorService
      .delete(id, resourceId, resourceTypeId)
      .pipe(takeUntil(destroy$))
      .subscribe({
        next: () => {
          this.toastService.success('Property descriptor deleted successfully.');
          onSuccess();
          this.descriptorToDelete.set(null);
          this.isDeleting.set(false);
          modal.close();
        },
        error: (err) => {
          this.isDeleting.set(false);

          // If err is undefined, the interceptor already handled it - assume it's a conflict
          if (!err) {
            const descriptor = this.descriptorToDelete();
            if (descriptor) {
              this.descriptorToDelete.set({
                ...descriptor,
                errorMessage: 'This property descriptor is still in use and cannot be deleted.',
              });
            }
            return;
          }

          // Check for 409 Conflict status or error message indicating force delete is needed
          const needsForceDelete =
            err.status === 409 ||
            err.message?.includes('marked to be deleted') ||
            err.message?.includes('still in use') ||
            err.message?.includes('cannot be deleted');

          if (needsForceDelete) {
            // Update descriptor with error message to show force delete option
            const descriptor = this.descriptorToDelete();
            if (descriptor) {
              const errorMsg =
                err.error?.message || err.message || 'This property descriptor is still in use and cannot be deleted.';
              this.descriptorToDelete.set({ ...descriptor, errorMessage: errorMsg });
            }
          } else {
            this.toastService.error(
              'Failed to delete property descriptor: ' + (err.error?.message || err.message || 'Unknown error'),
            );
            modal.close();
          }
        },
      });
  }

  private performForceDelete(
    id: number,
    resourceId: number | undefined,
    resourceTypeId: number | undefined,
    modal: any,
    onSuccess: () => void,
    destroy$: Subject<void>,
  ) {
    this.descriptorService
      .forceDelete(id, resourceId, resourceTypeId)
      .pipe(takeUntil(destroy$))
      .subscribe({
        next: () => {
          this.toastService.success('Property descriptor force deleted successfully.');
          onSuccess();
          this.descriptorToDelete.set(null);
          this.isDeleting.set(false);
          modal.close();
        },
        error: (err) => {
          this.toastService.error(
            'Failed to force delete property descriptor: ' + (err.error?.message || err.message || 'Unknown error'),
          );
          this.isDeleting.set(false);
          modal.close();
        },
      });
  }
}
