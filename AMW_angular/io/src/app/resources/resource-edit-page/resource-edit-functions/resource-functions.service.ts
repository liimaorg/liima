import { BaseService } from '../../../base/base.service';
import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { ResourceFunction } from './resource-function';
import { catchError, shareReplay, switchMap } from 'rxjs/operators';
import { Observable, startWith, Subject } from 'rxjs';
import { Resource } from '../../../resource/resource';
import { toSignal } from '@angular/core/rxjs-interop';

@Injectable({ providedIn: 'root' })
export class ResourceFunctionsService extends BaseService {
  private path = `${this.getBaseUrl()}/resources/functions`;
  private functions$: Subject<Number> = new Subject<Number>();

  private functionById$: Observable<ResourceFunction[]> = this.functions$.pipe(
    switchMap((id: number) => this.getResourceFunctions(id)),
    shareReplay(1),
  );

  functions = toSignal(this.functionById$, { initialValue: [] });

  constructor(private http: HttpClient) {
    super();
  }

  setIdForFunctionList(id: number) {
    this.functions$.next(id);
  }

  getResourceFunctions(id: number): Observable<ResourceFunction[]> {
    return this.http
      .get<ResourceFunction[]>(`${this.path}/${id}`, {
        headers: this.getHeaders(),
      })
      .pipe(catchError(this.handleError));
  }
}
