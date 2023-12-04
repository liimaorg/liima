import { Component, Input } from '@angular/core';

type SuccessOrError = 'success' | 'error';
@Component({
  selector: 'app-toast',
  template: `
    <div *ngIf="show" class="toast" role="alert" aria-live="assertive" aria-atomic="true">
      <div
        class="toast-image"
        [ngClass]="{ 'image-success': type === 'success', 'image-error': type === 'error' }"
      ></div>
      <div>
        <div
          class="toast-header"
          [ngClass]="{ 'header-success': type === 'success', 'header-error': type === 'error' }"
        >
          <strong class="mr-auto">Information</strong>
          <i (click)="close()" class="close bi bi-x-circle"></i>
        </div>
        <div class="toast-body" [ngClass]="{ 'body-success': type === 'success', 'body-error': type === 'error' }">
          <ul class="toast-list">
            <li>{{ message }}</li>
          </ul>
        </div>
      </div>
    </div>
  `,
  styleUrls: ['./toast.component.scss'],
})
export class ToastComponent {
  @Input() message = '';
  @Input() type: SuccessOrError;
  show = false;

  public display(message: string, type: SuccessOrError = 'success', duration = 3000) {
    this.message = message;
    this.type = type;
    this.show = true;

    setTimeout(() => {
      this.show = false;
    }, duration);
  }

  close() {
    this.show = false;
  }
}
