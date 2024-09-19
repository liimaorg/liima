import { inject, Injectable } from '@angular/core';
import { BaseService } from '../base/base.service';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { AppServer } from './app-server';

@Injectable({ providedIn: 'root' })
export class AppsService extends BaseService {
  private http = inject(HttpClient);
  appsUrl = `${this.getBaseUrl()}/apps`;

  constructor() {
    super();
  }

  getApps(offset: number, limit: number, filter: string, releaseId: number): Observable<AppServer[]> {
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
      })
      .pipe(catchError(this.handleError));
  }
}
