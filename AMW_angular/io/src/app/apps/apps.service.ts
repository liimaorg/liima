import { inject, Injectable, signal, WritableSignal } from '@angular/core';
import { BaseService } from '../base/base.service';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable, of, startWith, Subject } from 'rxjs';
import { catchError, map, shareReplay, switchMap } from 'rxjs/operators';
import { AppServer } from './app-server';
import { toSignal } from '@angular/core/rxjs-interop';
import { AppCreate } from './app-create';

@Injectable({ providedIn: 'root' })
export class AppsService extends BaseService {
  private http = inject(HttpClient);
  private appsUrl = `${this.getBaseUrl()}/apps`;

  private reload$ = new Subject<AppServer[]>();

  filter = signal<string>(null);
  releaseId: WritableSignal<number | undefined> = signal(undefined);
  private apps$: Observable<AppServer[]> = this.reload$.pipe(
    switchMap(() => this.getApps(this.filter(), this.releaseId())),
    startWith(null),
    shareReplay(1),
  );
  apps = toSignal(this.apps$, { initialValue: null as AppServer[] });

  constructor() {
    super();
  }

  refreshData() {
    this.reload$.next([]);
  }

  private getApps(filter: string, releaseId: number | undefined) {
    if (!releaseId) return of([]);

    let urlParams = '';
    if (filter !== undefined && filter !== null && filter !== '') {
      urlParams += `appServerName=${filter}&`;
    }

    return this.http
      .get<AppServer[]>(`${this.appsUrl}?${urlParams}releaseId=${releaseId}`, {
        headers: this.getHeaders(),
        observe: 'response',
      })
      .pipe(catchError(this.handleError))
      .pipe(
        map((response: HttpResponse<AppServer[]>) => {
          return response.body;
        }),
      );
  }

  createAppServer(appServer: AppServer) {
    return this.http
      .post<string>(`${this.appsUrl}/appServer?appServerName=${appServer.name}&releaseId=${appServer.release.id}`, {
        headers: this.getHeaders(),
      })
      .pipe(catchError(this.handleError));
  }

  createApp(app: AppCreate) {
    if (app.appServerId) {
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
