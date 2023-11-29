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
          <button
            type="button"
            class="close"
            aria-label="Close"
            (click)="close()"
            [ngClass]="{ 'close-success': type === 'success', 'close-error': type === 'error' }"
          >
            &times;
          </button>
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
  @Input() message: string = '';
  @Input() type: SuccessOrError;
  show: boolean = false;

  public display(message: string, type: SuccessOrError = 'success') {
    this.message = message;
    this.type = type;
    this.show = true;

    setTimeout(() => {
      this.show = false;
    }, 10000);
  }

  close() {
    this.show = false;
  }
}
