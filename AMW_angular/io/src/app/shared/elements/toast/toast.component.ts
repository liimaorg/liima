import {Component, Input} from '@angular/core';

@Component({
  selector: 'app-toast',
  template: `
    <div *ngIf="show" class="toast" role="alert" aria-live="assertive" aria-atomic="true">
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
  `,
  styles: [`
    .toast {
      z-index: 1050;
      display: block !important;
      width: auto;
      padding: 0.25rem;
      position: fixed;
      right: 2rem;
      bottom: 2.5rem;
      background-color: white;
    }

    .toast-body {
      color: #127e94;
      padding-top: 0;
    }

    .toast-list {
      list-style-type: disc;
      padding-left: 1rem;
      margin-bottom: 0;
    }

    .toast-header {
      justify-content: space-between;
      padding-top: 0.5rem;
      padding-bottom: 0;
      color: #127e94;
      border-bottom: 0;
    }

    .close {
      font-size: 1rem;
      line-height: 1;
      color: #325d9d;
      padding-bottom: 0;
      border-radius: 50%;
      border: 1.5px solid #325d9d;
    }
  `]
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
