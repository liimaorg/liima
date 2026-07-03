import { Component, effect, inject, input } from '@angular/core';
import { LoadingTrackerService } from 'src/app/shared/elements/loading-tracker.service';

@Component({
  selector: 'app-loading-indicator',
  template: `
    @if (loadingTracker.isOverlayOwner(token)) {
      <div class="d-flex justify-content-center align-items-center overlay">
        <div class="spinner-border text-light" role="status">
          <span class="visually-hidden">Loading...</span>
        </div>
      </div>
    }
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
})
export class LoadingIndicatorComponent {
  protected loadingTracker = inject(LoadingTrackerService);
  isLoading = input.required<boolean>();
  protected readonly token = Symbol('loading-indicator');

  constructor() {
    effect(() => {
      if (this.isLoading()) {
        this.loadingTracker.startLoading(this.token);
      } else {
        this.loadingTracker.stopLoading(this.token);
      }
    });
  }
}
