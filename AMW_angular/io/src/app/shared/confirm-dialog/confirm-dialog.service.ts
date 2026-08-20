import { inject, Injectable } from '@angular/core';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { ConfirmDialogComponent } from './confirm-dialog.component';

@Injectable({ providedIn: 'root' })
export class ConfirmDialogService {
  private modalService = inject(NgbModal);

  confirm(
    message: string,
    options?: { title?: string; confirmLabel?: string; cancelLabel?: string },
  ): Promise<boolean> {
    const modalRef = this.modalService.open(ConfirmDialogComponent, { size: 'sm' });
    (modalRef.componentInstance as ConfirmDialogComponent).configure({ message, ...options });
    return modalRef.result.then(
      () => true,
      () => false,
    );
  }
}
