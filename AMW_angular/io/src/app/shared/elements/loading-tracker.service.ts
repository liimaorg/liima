import { Injectable, signal } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class LoadingTrackerService {
  private activeLoaders = signal(0);
  private overlayOwner = signal<symbol | null>(null);

  startLoading(token: symbol) {
    let count = 0;
    this.activeLoaders.update((n) => (count = n + 1));
    if (count === 1) {
      this.overlayOwner.set(token);
    }
  }

  stopLoading(token: symbol) {
    let count = 0;
    this.activeLoaders.update((n) => (count = Math.max(0, n - 1)));
    if (count === 0) {
      this.overlayOwner.set(null);
    }
  }

  isOverlayOwner(token: symbol): boolean {
    return this.overlayOwner() === token;
  }
}
