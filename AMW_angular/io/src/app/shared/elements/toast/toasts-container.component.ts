import { Component } from '@angular/core';
import { NgbToastModule } from '@ng-bootstrap/ng-bootstrap';
import { NgTemplateOutlet } from '@angular/common';
import { ToastService } from './toast-service';

@Component({
  selector: 'app-toasts',
  standalone: true,
  imports: [NgbToastModule, NgTemplateOutlet],
  template: `@for (toast of toastService.toasts; track toast) {
    <ngb-toast
      [class]="'toast ' + toast.type"
      [autohide]="true"
      [delay]="toast.delay || 5000"
      (hidden)="toastService.remove(toast)"
    >
      <div class="toast-content">
        <div class="toast-image"></div>
        <div class="px-4 align-self-center">{{ toast.body }}</div>
      </div>
    </ngb-toast>
    }`,
  styles: `
    :host {
      position: fixed;
      bottom: 3rem;
      right: 0;
      z-index: 1200;
      margin: 0.5rem;
    }

    .toast {
      margin-bottom: 0.25rem;
    }

    .toast-content {
      display: flex;
      flex-direction: row;
    }

    .toast-image {
      background-image: url('toast_alert.png');
      background-repeat: no-repeat;
      height: 50px;
      width: 50px;
      border-radius: var(--bs-border-radius) 0 0 var(--bs-border-radius);
    }

    .light .toast-image {
      background-color: var(--bs-primary);
    }
    .danger .toast-image {
      background-color: var(--bs-danger);
    }

    .light .toast-content {
      color: steelblue;
    }

    .danger .toast-content {
      color: darkred;
    }
  `,
})
export class ToastsContainerComponent {
  constructor(public toastService: ToastService) {}
}
