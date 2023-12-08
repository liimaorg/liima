import { Component, Input } from '@angular/core';
import { NgIf } from '@angular/common';

@Component({
    selector: 'app-loading-indicator',
    template: `
    <div
      *ngIf="isLoading"
      class="d-flex justify-content-center align-items-center overlay"
    >
      <div class="spinner-border text-light" role="status">
        <span class="visually-hidden">Loading...</span>
      </div>
    </div>
    <div class=""></div>
  `,
    styles: [
        `
      .overlay {
        opacity: 0.5;
        background: #000;
        width: 100%;
        height: 100%;
        z-index: 9999;
        top: 0;
        left: 0;
        position: fixed !important;
      }
    `,
    ],
    standalone: true,
    imports: [NgIf],
})
export class LoadingIndicatorComponent {
  @Input()
  isLoading: boolean;

  constructor() {}
}
