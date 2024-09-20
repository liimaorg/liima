import { inject, Injectable } from '@angular/core';
import { BaseService } from '../base/base.service';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { AppServer } from './app-server';
import { App } from './app';
import { Release } from '../settings/releases/release';

@Injectable({ providedIn: 'root' })
export class AppsService extends BaseService {
  private http = inject(HttpClient);
  appsUrl = `${this.getBaseUrl()}/apps`;

  constructor() {
    super();
  }

  getApps(offset: number, limit: number, filter: string, releaseId: number): Observable<HttpResponse<AppServer[]>> {
    let urlParams = '';
    if (offset != null) {
      urlParams = `start=${offset}&`;
    }

    if (limit != null) {
      urlParams += `limit=${limit}&`;
    }

    if (filter != null) {
      urlParams += `appServerName=${filter}&`;
    }

    return this.http
      .get<any[]>(`${this.appsUrl}?${urlParams}releaseId=${releaseId}`, {
        headers: this.getHeaders(),
        observe: 'response',
      })
      .pipe(catchError(this.handleError));
  }

  create(appServer: AppServer) {
    return this.http
      .post<AppServer>(`${this.appsUrl}`, appServer, {
        headers: this.getHeaders(),
      })
      .pipe(catchError(this.handleError));
  }
}
