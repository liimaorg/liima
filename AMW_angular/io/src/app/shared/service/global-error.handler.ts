import { ErrorHandler, Injectable, inject, Provider } from '@angular/core';
import { ToastService } from '../elements/toast/toast.service';

export function provideGlobalErrorHandler(): Provider[] {
  return [{ provide: ErrorHandler, useClass: GlobalErrorHandler }];
}

@Injectable({ providedIn: 'root' })
export class GlobalErrorHandler implements ErrorHandler {
  toastService = inject(ToastService);
  handleError(error: Error): void {
    if (error.stack) {
      console.error('GlobalErrorHandler: ' + error.stack);
    } else {
      console.error('GlobalErrorHandler: ' + error.message);
    }
    this.toastService.error(error.message);
  }
}
