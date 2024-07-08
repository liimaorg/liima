import { ErrorHandler, Injectable, inject, Provider } from '@angular/core';
import { ToastService } from '../elements/toast/toast.service';

export function provideGlobalErrorHandler(): Provider[] {
  return [{ provide: ErrorHandler, useClass: GlobalErrorHandler }];
}

@Injectable({ providedIn: 'root' })
export class GlobalErrorHandler implements ErrorHandler {
  toastService = inject(ToastService);
  handleError(error: { message: string }): void {
    console.error('Global Error Handler: ' + error);
    this.toastService.error(error.message);
  }
}
