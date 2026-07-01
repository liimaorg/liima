import { Injectable, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';

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

  success(message: unknown) {
    this.show({ type: 'success', body: this.toMessage(message), delay: DEFAULT_SUCCESS_TIMEOUT });
  }

  error(message: unknown) {
    this.show({ type: 'error', body: this.toMessage(message), delay: DEFAULT_ERROR_TIMEOUT });
  }

  private toMessage(message: unknown): string {
    if (typeof message === 'string') {
      return message;
    }
    if (message instanceof HttpErrorResponse) {
      return message.error?.message ?? message.message ?? 'An error occurred';
    }
    if (message instanceof Error) {
      return message.message;
    }
    if (message && typeof message === 'object') {
      const obj = message as { error?: { message?: string }; message?: string };
      return obj.error?.message ?? obj.message ?? 'An error occurred';
    }
    return message != null ? String(message) : 'An error occurred';
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
