import { Injectable } from '@angular/core';
import { BaseService } from '../../base/base.service';
import { HttpClient } from '@angular/common/http';
import { AppFunction } from './appFunction';
import { Observable, startWith, Subject } from 'rxjs';
import { toSignal } from '@angular/core/rxjs-interop';
import { shareReplay, switchMap } from 'rxjs/operators';
import {RevisionInformation} from "./revisionInformation";

@Injectable({ providedIn: 'root' })
export class FunctionsService extends BaseService {
  private reload$ = new Subject<AppFunction[]>();
  private function$ = this.reload$.pipe(
    startWith(null),
    switchMap(() => this.getAllFunctions()),
    shareReplay(1),
  );
  functions = toSignal(this.function$, { initialValue: [] as AppFunction[] });

  constructor(private http: HttpClient) {
    super();
  }

  getAllFunctions(): Observable<AppFunction[]> {
    return this.http.get<AppFunction[]>(`${this.getBaseUrl()}/functions`);
  }

  addNewFunction(newFunction: AppFunction): Observable<void> {
    return this.http.post<void>(`${this.getBaseUrl()}/functions`, newFunction);
  }

  modifyFunction(newFunction: AppFunction): Observable<void> {
    return this.http.put<void>(`${this.getBaseUrl()}/functions`, newFunction);
  }

  deleteFunction(id: number): Observable<void> {
    return this.http.delete<void>(`${this.getBaseUrl()}/functions/${id}`);
  }

  getFunctionRevisions(id: number): Observable<RevisionInformation[]> {
    return this.http.get<RevisionInformation[]>(`${this.getBaseUrl()}/functions/${id}/revisions`);
  }

  getFunctionByIdAndRevision(id: number, revisionId: number): Observable<AppFunction> {
    return this.http.get<AppFunction>(`${this.getBaseUrl()}/functions/${id}/revisions/${revisionId}`);
  }

  refreshData() {
    this.reload$.next([]);
  }
}
