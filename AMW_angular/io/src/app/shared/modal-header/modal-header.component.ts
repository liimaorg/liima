import { Component, input, output } from '@angular/core';

@Component({
  selector: 'app-modal-header',
  standalone: true,
  imports: [],
  templateUrl: './modal-header.component.html',
})
export class ModalHeaderComponent {
  title = input<string>();
  cancel = output<void>();

  onCancel(): void {
    this.cancel.emit();
  }
}
