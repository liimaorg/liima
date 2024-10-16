import { BaseService } from '../base/base.service';
import { catchError, map } from 'rxjs/operators';
import { inject, Injectable, Signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Server } from './server';
import { toSignal } from '@angular/core/rxjs-interop';

@Injectable({ providedIn: 'root' })
export class ServersService extends BaseService {
  private http = inject(HttpClient);
  private serversUrl = `${this.getBaseUrl()}/servers`;

  private servers$ = this.http
    .get<Server[]>(`${this.serversUrl}`, {
      headers: this.getHeaders(),
      observe: 'response',
    })
    .pipe(catchError(this.handleError))
    .pipe(map((response) => response.body));

  servers: Signal<Server[]> = toSignal(this.servers$);
}
