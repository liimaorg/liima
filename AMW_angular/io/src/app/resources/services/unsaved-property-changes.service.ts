import { Injectable, computed, signal } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class UnsavedPropertyChangesService {
  private dirtyKeys = signal<Set<string>>(new Set());
  private discardToken = signal(0);

  hasUnsavedChanges = computed(() => this.dirtyKeys().size > 0);
  discardChangesToken = this.discardToken.asReadonly();

  setDirty(key: string, dirty: boolean) {
    this.dirtyKeys.update((set) => {
      const next = new Set(set);
      if (dirty) {
        next.add(key);
      } else {
        next.delete(key);
      }
      return next;
    });
  }

  clearAll() {
    this.dirtyKeys.set(new Set());
  }

  discardAll() {
    this.clearAll();
    this.discardToken.update((v) => v + 1);
  }
}
