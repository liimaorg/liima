import { Component, input, output } from '@angular/core';

@Component({
  selector: 'app-modal-header',
  standalone: true,
  imports: [],
  template: `<div class="modal-header">
    <h5 class="modal-title">{{ title() }}</h5>
    <button type="button" class="btn btn-light close" aria-label="Close" (click)="onCancel()">
      <span aria-hidden="true">&times;</span>
    </button>
  </div> `,
})
export class ModalHeaderComponent {
  title = input<string>();
  cancel = output<void>();

  onCancel(): void {
    this.cancel.emit();
  }
}
