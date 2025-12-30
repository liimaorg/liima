import { enableProdMode, provideZonelessChangeDetection } from '@angular/core';

import { environment } from './environments/environment';
import { AppComponent } from './app/app.component';
import { withInterceptorsFromDi, provideHttpClient } from '@angular/common/http';
import { bootstrapApplication } from '@angular/platform-browser';
import { provideRouter, withHashLocation } from '@angular/router';
import { routes } from './app/app.routes';
import { provideGlobalErrorHandler } from './app/shared/service/global-error.handler';
import { provideHttpToastInterceptor } from './app/shared/interceptors/http-toast.interceptor';

if (environment.production) {
  enableProdMode();
}

bootstrapApplication(AppComponent, {
  providers: [
    provideZonelessChangeDetection(),
    provideRouter(routes, withHashLocation()),
    provideHttpClient(withInterceptorsFromDi()),
    provideGlobalErrorHandler(),
    provideHttpToastInterceptor(),
  ],
}).catch((err) => console.error(err));
