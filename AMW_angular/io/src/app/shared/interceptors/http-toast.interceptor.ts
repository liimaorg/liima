import { inject, Injectable, Provider } from '@angular/core';
import { HTTP_INTERCEPTORS, HttpErrorResponse, HttpHandler, HttpInterceptor, HttpRequest } from '@angular/common/http';
import { catchError, throwError } from 'rxjs';
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
        console.error('Error in HttpToastInterceptor', error);
        // Don't show toast for 422 - these are validation errors with structured data
        if (error.status !== 422 && error.error?.message) {
          this.toastService.error(error.error.message);
        }
        // Re-throw the error so services can handle it
        return throwError(() => error);
      }),
    );
  }
}
