import { Injectable, signal } from '@angular/core';

export interface Toast {
  type: 'success' | 'error';
  body: string;
  delay?: number;
}

const DEFAULT_SUCCESS_TIMEOUT = 5000;
const DEFAULT_ERROR_TIMEOUT = 15000;

@Injectable({ providedIn: 'root' })
export class ToastService {
  toasts = signal<Toast[]>([]);

  success(message: string) {
    this.show({ type: 'success', body: message, delay: DEFAULT_SUCCESS_TIMEOUT });
  }

  error(message: string) {
    this.show({ type: 'error', body: message, delay: DEFAULT_ERROR_TIMEOUT });
  }

  show(toast: Toast) {
    setTimeout(() => {
      this.toasts.update((toasts) => [...toasts, toast]);
    });
  }

  remove(toast: Toast) {
    this.toasts.update((toasts) => toasts.filter((t) => t !== toast));
  }
}
