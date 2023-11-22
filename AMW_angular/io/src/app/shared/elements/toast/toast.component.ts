import {Component, Input} from '@angular/core';
import { NgIf } from '@angular/common';

@Component({
    selector: 'app-toast',
    template: `
    <div *ngIf="show" class="toast" role="alert" aria-live="assertive" aria-atomic="true">
      <div class="toast-image">
      </div>
      <div>
        <div class="toast-header">
          <strong class="mr-auto">Information</strong>
          <button type="button" class="ml-2 mb-1 close" aria-label="Close" (click)="close()">
            <span aria-hidden="true">&times;</span>
          </button>
        </div>
        <div class="toast-body">
          <ul class="toast-list">
            <li>{{ message }}</li>
          </ul>
        </div>
      </div>
    </div>
  `,
    styleUrls: ['./toast.component.scss'],
    standalone: true,
    imports: [NgIf]
})
export class ToastComponent {
  @Input() message: string = '';
  show: boolean = false;

  public display(message: string, duration: number = 3000) {
    this.message = message;
    this.show = true;

    setTimeout(() => {
      this.show = false;
    }, duration);
  }

  close() {
    this.show = false;
  }
}
