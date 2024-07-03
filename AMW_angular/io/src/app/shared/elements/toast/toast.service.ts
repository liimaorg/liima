import { Injectable } from '@angular/core';

export interface Toast {
  type: 'success' | 'error';
  body: string;
  delay?: number;
}

const DEFAULT_SUCCESS_TIMEOUT = 5000;
const DEFAULT_ERROR_TIMEOUT = 15000;

@Injectable({ providedIn: 'root' })
export class ToastService {
  toasts: Toast[] = [];

  success(message: string) {
    this.show({ type: 'success', body: message, delay: DEFAULT_SUCCESS_TIMEOUT });
  }

  error(message: string) {
    this.show({ type: 'error', body: message, delay: DEFAULT_ERROR_TIMEOUT });
  }

  show(toast: Toast) {
    this.toasts.push(toast);
  }

  remove(toast: Toast) {
    this.toasts = this.toasts.filter((t) => t !== toast);
  }
}
