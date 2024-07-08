import { Component, Input } from '@angular/core';
import { NgIf } from '@angular/common';

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
  imports: [NgIf],
})
export class NotificationComponent {
  @Input()
  message: string;

  @Input()
  messageType: MessageType;

  constructor() {}
}

export type MessageType = 'success' | 'warning';
