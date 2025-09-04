import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-notification',
  template: `
    @if (message) {
      <div class="alert alert-{{ messageType }}" role="alert">
        <span [innerHTML]="message"></span
        ><button type="button" class="btn-close float-end" (click)="message = ''" aria-label="Close"></button>
      </div>
    }
  `,
  standalone: true,
})
export class NotificationComponent {
  @Input()
  message: string;

  @Input()
  messageType: MessageType;
}

export type MessageType = 'success' | 'warning';
