import { Injectable } from '@angular/core';
import { BaseService } from '../../base/base.service';
import { HttpClient } from '@angular/common/http';
import { Function } from './function';
import { Observable, startWith, Subject } from 'rxjs';
import { toSignal } from '@angular/core/rxjs-interop';
import { shareReplay, switchMap } from 'rxjs/operators';

@Injectable({ providedIn: 'root' })
export class FunctionsService extends BaseService {
  private reload$ = new Subject<Function[]>();
  private function$ = this.reload$.pipe(
    startWith(null),
    switchMap(() => this.getAllFunctions()),
    shareReplay(1),
  );
  functions = toSignal(this.function$, { initialValue: [] as Function[] });

  constructor(private http: HttpClient) {
    super();
  }

  getAllFunctions(): Observable<Function[]> {
    return this.http.get<Function[]>(`${this.getBaseUrl()}/functions`);
  }

  addNewFunction(newFunction: Function): Observable<void> {
    return this.http.post<void>(`${this.getBaseUrl()}/functions`, newFunction);
  }

  modifyFunction(newFunction: Function): Observable<void> {
    return this.http.put<void>(`${this.getBaseUrl()}/functions`, newFunction);
  }
  deleteFunction(id: number): Observable<void> {
    return this.http.delete<void>(`${this.getBaseUrl()}/functions/${id}`);
  }

  refreshData() {
    this.reload$.next([]);
  }
}
