import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { ResourceFunction } from './resource-function';
import { catchError, shareReplay, switchMap } from 'rxjs/operators';
import { Observable, Subject } from 'rxjs';

import { toSignal } from '@angular/core/rxjs-interop';
import { BaseService } from '../base/base.service';
import { RevisionInformation } from '../shared/model/revisionInformation';

@Injectable({ providedIn: 'root' })
export class ResourceFunctionsService extends BaseService {
  private path = `${this.getBaseUrl()}/resources/functions`;
  private functions$: Subject<Number> = new Subject<Number>();
  private functionsForType$: Subject<Number> = new Subject<Number>();

  private functionById$: Observable<ResourceFunction[]> = this.functions$.pipe(
    switchMap((id: number) => this.getResourceFunctions(id)),
    shareReplay(1),
  );

  private functionByTypeId$: Observable<ResourceFunction[]> = this.functionsForType$.pipe(
    switchMap((id: number) => this.getResourceTypeFunctions(id)),
    shareReplay(1),
  );

  functions = toSignal(this.functionById$, { initialValue: [] });
  functionsForType = toSignal(this.functionByTypeId$, { initialValue: [] });

  constructor(private http: HttpClient) {
    super();
  }

  setIdForResourceFunctionList(id: number) {
    this.functions$.next(id);
  }

  setIdForResourceTypeFunctionList(id: number) {
    this.functionsForType$.next(id);
  }

  getResourceFunctions(id: number): Observable<ResourceFunction[]> {
    return this.http
      .get<ResourceFunction[]>(`${this.path}/resource/${id}`, {
        headers: this.getHeaders(),
      })
      .pipe(catchError(this.handleError));
  }

  getResourceTypeFunctions(id: number): Observable<ResourceFunction[]> {
    return this.http
      .get<ResourceFunction[]>(`${this.path}/resourceType/${id}`, {
        headers: this.getHeaders(),
      })
      .pipe(catchError(this.handleError));
  }

  getFunctionRevisions(id: number): Observable<RevisionInformation[]> {
    return this.http.get<RevisionInformation[]>(`${this.path}/${id}/revisions`);
  }

  getFunctionByIdAndRevision(id: number, revisionId: number): Observable<ResourceFunction> {
    return this.http.get<ResourceFunction>(`${this.path}/${id}/revisions/${revisionId}`);
  }

  createFunctionForResource(id: number, func: ResourceFunction) {
    const jsonSet = new SerializableSet();
    func.miks.forEach((value) => jsonSet.add(value));
    func.miks = jsonSet;

    return this.http
      .post<ResourceFunction>(`${this.path}/resource/${id}`, func, {
        headers: this.getHeaders(),
      })
      .pipe(catchError(this.handleError));
  }
}
export class SerializableSet extends Set {
  toJSON() {
    return Array.from(this);
  }
}
