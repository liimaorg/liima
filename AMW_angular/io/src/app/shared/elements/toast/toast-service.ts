import { Injectable } from '@angular/core';

export interface Toast {
  type: 'success' | 'error';
  body: string;
  delay?: number;
}

@Injectable({ providedIn: 'root' })
export class ToastService {
  toasts: Toast[] = [];

  success(message: string) {
    this.show({ type: 'success', body: message });
  }

  error(message: string) {
    this.show({ type: 'error', body: message, delay: 15000 });
  }

  show(toast: Toast) {
    this.toasts.push(toast);
  }

  remove(toast: Toast) {
    this.toasts = this.toasts.filter((t) => t !== toast);
  }
}
