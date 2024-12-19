import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { AppConfiguration } from './app-configuration';
import { BaseService } from '../base/base.service';
import { AppInformation } from './app-information';

@Injectable({ providedIn: 'root' })
export class SettingService extends BaseService {
  constructor(private http: HttpClient) {
    super();
  }

  getAllAppSettings(): Observable<AppConfiguration[]> {
    return this.http
      .get<AppConfiguration[]>(`${this.getBaseUrl()}/settings`, {
        headers: this.getHeaders(),
      })
      .pipe(catchError(this.handleError));
  }

  getAppInformation(): Observable<AppInformation[]> {
    return this.http.get<AppInformation[]>(`${this.getBaseUrl()}/settings/appInfo`).pipe(catchError(this.handleError));
  }
}
