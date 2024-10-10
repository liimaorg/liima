import { inject, Injectable, signal } from '@angular/core';
import { BaseService } from '../base/base.service';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable, startWith, Subject } from 'rxjs';
import { catchError, map, shareReplay, switchMap } from 'rxjs/operators';
import { AppServer } from './app-server';
import { toSignal } from '@angular/core/rxjs-interop';
import { AppCreate } from './app-create';

@Injectable({ providedIn: 'root' })
export class AppsService extends BaseService {
  private http = inject(HttpClient);
  private appsUrl = `${this.getBaseUrl()}/apps`;

  private reload$ = new Subject<AppServer[]>();

  offset = signal(0);
  limit = signal(20);
  filter = signal<string>(null);
  releaseId = signal(50);
  private apps$: Observable<AppServer[]> = this.reload$.pipe(
    startWith(null),
    switchMap(() => this.getApps(this.offset(), this.limit(), this.filter(), this.releaseId())),
    shareReplay(1),
  );
  count = signal(0);
  apps = toSignal(this.apps$, { initialValue: [] as AppServer[] });

  constructor() {
    super();
  }

  refreshData() {
    this.reload$.next([]);
  }

  private getApps(offset: number, limit: number, filter: string, releaseId: number) {
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
      .pipe(catchError(this.handleError))
      .pipe(
        map((response: HttpResponse<AppServer[]>) => {
          this.count.set(Number(response.headers.get('x-total-count')));
          return response.body;
        }),
      );
  }

  createAppServer(appServer: AppServer) {
    return this.http
      .post<any[]>(`${this.appsUrl}/appServer?appServerName=${appServer.name}&releaseId=${appServer.release.id}`, {
        headers: this.getHeaders(),
      })
      .pipe(catchError(this.handleError));
  }

  createApp(app: AppCreate) {
    if (app.appServerId) {
      debugger;
      return this.http
        .post<AppCreate>(`${this.appsUrl}/appWithServer`, app, {
          headers: this.getHeaders(),
        })
        .pipe(catchError(this.handleError));
    }

    return this.http
      .post<AppCreate>(`${this.appsUrl}?appName=${app.appName}&releaseId=${app.appReleaseId}`, app, {
        headers: this.getHeaders(),
      })
      .pipe(catchError(this.handleError));
  }
}
