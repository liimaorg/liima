import {Component, Input} from '@angular/core';

@Component({
  selector: 'app-toast',
  template: `
    <div *ngIf="show" class="toast" role="alert" aria-live="assertive" aria-atomic="true">
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

    }

    .toast-body {
      color: #127e94;
    }

    .toast-list {
      list-style-type: disc;
      padding-left: 1rem;
      margin-bottom: 0;
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
}
