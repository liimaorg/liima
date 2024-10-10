import { inject, Injectable, Provider } from '@angular/core';
import { HTTP_INTERCEPTORS, HttpHandler, HttpInterceptor, HttpRequest } from '@angular/common/http';
import { catchError, EMPTY } from 'rxjs';
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
      catchError((error) => {
        console.error('Error in HttpToastInterceptor', error);
        this.toastService.error(error.error.message);
        return EMPTY;
      }),
    );
  }
}
