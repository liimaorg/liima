import { inject, Injectable, signal } from '@angular/core';
import { BaseService } from '../base/base.service';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { catchError, tap } from 'rxjs/operators';
import { Tag } from '../settings/tags/tag';

@Injectable({ providedIn: 'root' })
export class AppsService extends BaseService {
  private http = inject(HttpClient);
  appsUrl = `${this.getBaseUrl()}/apps`;
  //
  // apps = signal<any[]>([]);
  // private apps$ = this.getApps(0, 100).pipe(tap(apps)) => this.apps.set(apps)));
  //
  constructor() {
    super();
  }

  getApps(offset: number, limit: number): Observable<any[]> {
    return this.http
      .get<any[]>(`${this.appsUrl}?start=${offset}&limit=${limit}`, {
        headers: this.getHeaders(),
      })
      .pipe(catchError(this.handleError));
  }
}
