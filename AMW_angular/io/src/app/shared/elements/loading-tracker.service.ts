import { Injectable, signal } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class LoadingTrackerService {
  private activeLoaders = signal<Set<symbol>>(new Set());
  private overlayOwner = signal<symbol | null>(null);

  startLoading(token: symbol) {
    this.activeLoaders.update((loaders) => {
      if (loaders.has(token)) return loaders;
      const next = new Set(loaders);
      next.add(token);
      return next;
    });
    if (this.overlayOwner() === null) {
      this.overlayOwner.set(token);
    }
  }

  stopLoading(token: symbol) {
    this.activeLoaders.update((loaders) => {
      if (!loaders.has(token)) return loaders;
      const next = new Set(loaders);
      next.delete(token);
      return next;
    });
    if (this.overlayOwner() === token) {
      const remaining = this.activeLoaders();
      this.overlayOwner.set(remaining.values().next().value ?? null);
    }
  }

  isOverlayOwner(token: symbol): boolean {
    return this.overlayOwner() === token;
  }
}
