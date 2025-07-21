import { BaseService } from '../base/base.service';
import { catchError, map, shareReplay, switchMap } from 'rxjs/operators';
import { inject, Injectable, signal, Signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Server } from './server';
import { toSignal } from '@angular/core/rxjs-interop';
import { Observable, startWith, Subject } from 'rxjs';
import { Resource } from '../resources/models/resource';
import { isServerFilterEmpty, ServerFilter } from './servers-filter/server-filter';

@Injectable({ providedIn: 'root' })
export class ServersService extends BaseService {
  private http = inject(HttpClient);
  private serversUrl = `${this.getBaseUrl()}/servers`;
  public serverFilter = signal<ServerFilter>(null);
  private serverFilter$: Subject<ServerFilter> = new Subject<ServerFilter>();
  private servers$: Observable<Server[]> = this.serverFilter$.pipe(
    switchMap((filter: ServerFilter) => this.getServers(filter)),
    startWith(null),
    shareReplay(1),
  );

  servers: Signal<Server[]> = toSignal(this.servers$, { initialValue: [] as Server[] });

  private runtimes$ = this.http
    .get<Resource[]>(`${this.serversUrl}/runtimes`)
    .pipe(catchError(this.handleError))
    .pipe(map((data) => data.sort((a, b) => (a.name < b.name ? -1 : 1))));
  runtimes: Signal<Resource[]> = toSignal(this.runtimes$);

  private appServersSuggestions$ = this.http
    .get<string[]>(`${this.serversUrl}/appServersSuggestions`)
    .pipe(catchError(this.handleError));
  appServersSuggestions: Signal<string[]> = toSignal(this.appServersSuggestions$);

  getServers(filter?: ServerFilter): Observable<Server[]> {
    if (isServerFilterEmpty(filter)) {
      return this.http
        .get<Server[]>(`${this.serversUrl}`, {
          headers: this.getHeaders(),
          observe: 'response',
        })
        .pipe(catchError(this.handleError))
        .pipe(map((response) => response.body));
    } else {
      const urlParams = this.buildUrlParam(filter);
      return this.http.get<Server[]>(`${this.serversUrl}${urlParams}`).pipe(catchError(this.handleError));
    }
  }

  buildUrlParam(filter: ServerFilter) {
    let urlParams: string = `/filter?`;
    if (filter.environmentName && filter.environmentName !== 'All')
      urlParams += `environment=${filter.environmentName}`;
    if (filter.runtimeName && filter.runtimeName !== 'All') urlParams += `&runtime=${filter.runtimeName}`;
    if (filter.appServer) urlParams += `&appServer=${filter.appServer}`;
    if (filter.host) urlParams += `&host=${filter.host}`;
    if (filter.node) urlParams += `&node=${filter.node}`;
    return urlParams;
  }

  setServerFilter(filter: ServerFilter) {
    this.serverFilter.set(filter);
    this.serverFilter$.next(filter);
  }
}
