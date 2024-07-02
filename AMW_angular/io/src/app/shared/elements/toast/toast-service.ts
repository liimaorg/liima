import { Injectable } from '@angular/core';

export interface Toast {
  type: 'light' | 'danger';
  body: string;
  delay?: number;
}

@Injectable({ providedIn: 'root' })
export class ToastService {
  toasts: Toast[] = [];

  success(message: string) {
    this.show({ type: 'light', body: message });
  }

  error(message: string) {
    this.show({ type: 'danger', body: message, delay: 15000 });
  }

  show(toast: Toast) {
    this.toasts.push(toast);
  }

  remove(toast: Toast) {
    this.toasts = this.toasts.filter((t) => t !== toast);
  }
}
