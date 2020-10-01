import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-loading-indicator',
  template: `
    <div
      *ngIf="isLoading"
      class="d-flex justify-content-center align-items-center pt-5 w-100 h-100 infront bg-black-alpha-20 position-absolute overflow-auto"
    >
      <div class="spinner-border text-light" role="status">
        <span class="sr-only">Loading...</span>
      </div>
    </div>
    <div class=""></div>
  `,
  styles: [
    `
      .infront {
        z-index: 1000;
      }
      .bg-black-alpha-20 {
        background-color: rgba(0, 0, 0, 0.2);
      }
    `,
  ],
})
export class LoadingIndicatorComponent {
  @Input()
  isLoading: boolean;

  constructor() {}
}
