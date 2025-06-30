import { Component, input, output } from '@angular/core';

@Component({
  selector: 'app-modal-header',
  imports: [],
  template: `<div class="modal-header">
    <h5 class="modal-title">{{ title() }}</h5>
    <button type="button" class="btn btn-light close" aria-label="Close" (click)="onCancel()">
      <span>&times;</span>
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
