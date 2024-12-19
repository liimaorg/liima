import { BaseService } from '../../base/base.service';
import { inject, Injectable, Signal } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { catchError, map } from 'rxjs/operators';
import { toSignal } from '@angular/core/rxjs-interop';
import { Observable } from 'rxjs';
import { Config } from '../configuration';

@Injectable({ providedIn: 'root' })
export class ConfigurationService extends BaseService {
  private http = inject(HttpClient);
  private settingsUrl = `${this.getBaseUrl()}/settings`;

  private configuration$: Observable<Config[]> = this.http
    .get<Config[]>(`${this.settingsUrl}`, {
      headers: this.getHeaders(),
      observe: 'response',
    })
    .pipe(catchError(this.handleError))
    .pipe(map((response: HttpResponse<Config[]>): Config[] => response.body));

  configuration: Signal<Config[]> = toSignal(this.configuration$);
}
