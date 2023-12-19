import { Component, OnInit, Input } from '@angular/core';
import { NgIf } from '@angular/common';

@Component({
  selector: 'app-notification',
  template: `
    <div *ngIf="message" class="alert alert-{{ messageType }}" role="alert">
      <span [innerHTML]="message"></span
      ><button type="button" class="btn-close float-end" (click)="message = ''" aria-label="Close"></button>
    </div>
  `,
  standalone: true,
  imports: [NgIf],
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
