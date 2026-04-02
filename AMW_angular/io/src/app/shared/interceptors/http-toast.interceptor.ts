import { inject, Injectable, Provider } from '@angular/core';
import { HTTP_INTERCEPTORS, HttpErrorResponse, HttpHandler, HttpInterceptor, HttpRequest } from '@angular/common/http';
import { catchError, EMPTY, throwError } from 'rxjs';
import { ToastService } from '../elements/toast/toast.service';

export function provideHttpToastInterceptor(): Provider[] {
  return [{ provide: HTTP_INTERCEPTORS, useClass: HttpToastInterceptor, multi: true }];
}

@Injectable({
  providedIn: 'root',
})
export class HttpToastInterceptor implements HttpInterceptor {
  toastService = inject(ToastService);
  intercept(req: HttpRequest<any>, next: HttpHandler) {
    return next.handle(req).pipe(
      catchError((error: HttpErrorResponse) => {
        // Don't show toast for 422 - these are validation errors with structured data
        // Don't show toast for 409 - these are conflict errors that may need user confirmation (e.g., force delete)
        if (error.status === 422 || error.status === 409) {
          // Re-throw these errors so services can handle them
          return throwError(() => error);
        }

        // Show toast for other errors
        if (error.error?.message) {
          this.toastService.error(error.error.message);
          return EMPTY;
        }

        // Re-throw if no message to show
        return throwError(() => error);
      }),
    );
  }
}
