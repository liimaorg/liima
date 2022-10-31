import { Component, OnInit, Input } from '@angular/core';

@Component({
  selector: 'app-notification',
  template: `
    <div *ngIf="message" class="alert alert-{{ messageType }}" role="alert">
      <span [innerHTML]="message"></span
      ><button
        type="button"
        class="btn-close float-end"
        (click)="message = ''"
        aria-label="Close"
      >
      </button>
    </div>
  `,
})
export class NotificationComponent implements OnInit {
  @Input()
  message: string;

  @Input()
  messageType: MessageType;

  constructor() {}

  ngOnInit(): void {}
}

export type MessageType = 'success' | 'warning';
