import { Component, inject, signal } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { ButtonComponent } from '../button/button.component';

@Component({
  selector: 'app-confirm-dialog',
  standalone: true,
  imports: [ButtonComponent],
  templateUrl: './confirm-dialog.component.html',
})
export class ConfirmDialogComponent {
  activeModal = inject(NgbActiveModal);

  title = signal('Confirm');
  message = signal('');
  confirmLabel = signal('Confirm');
  cancelLabel = signal('Cancel');

  configure(config: { title?: string; message: string; confirmLabel?: string; cancelLabel?: string }): void {
    if (config.title) this.title.set(config.title);
    this.message.set(config.message);
    if (config.confirmLabel) this.confirmLabel.set(config.confirmLabel);
    if (config.cancelLabel) this.cancelLabel.set(config.cancelLabel);
  }

  onCancel(): void {
    this.activeModal.dismiss();
  }

  onConfirm(): void {
    this.activeModal.close(true);
  }
}
