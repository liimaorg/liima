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
  private path = `${this.getBaseUrl()}/resources`;
  private functions$: Subject<number> = new Subject<number>();
  private functionsForType$: Subject<number> = new Subject<number>();

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
      .get<ResourceFunction[]>(`${this.path}/resource/${id}/functions`, {
        headers: this.getHeaders(),
      })
      .pipe(catchError(this.handleError));
  }

  getResourceTypeFunctions(id: number): Observable<ResourceFunction[]> {
    return this.http
      .get<ResourceFunction[]>(`${this.path}/resourceType/${id}/functions`, {
        headers: this.getHeaders(),
      })
      .pipe(catchError(this.handleError));
  }

  getFunctionRevisions(id: number): Observable<RevisionInformation[]> {
    return this.http
      .get<RevisionInformation[]>(`${this.path}/functions/${id}/revisions`, {
        headers: this.getHeaders(),
      })
      .pipe(catchError(this.handleError));
  }

  getFunctionByIdAndRevision(id: number, revisionId: number): Observable<ResourceFunction> {
    return this.http
      .get<ResourceFunction>(`${this.path}/functions/${id}/revisions/${revisionId}`, {
        headers: this.getHeaders(),
      })
      .pipe(catchError(this.handleError));
  }

  createFunctionForResource(id: number, func: ResourceFunction) {
    const jsonSet = new SerializableSet();
    func.miks.forEach((value) => jsonSet.add(value));
    func.miks = jsonSet;

    return this.http
      .post<ResourceFunction>(`${this.path}/resource/${id}/functions`, func, {
        headers: this.getHeaders(),
      })
      .pipe(catchError(this.handleError));
  }

  createFunctionForResourceType(id: number, func: ResourceFunction) {
    const jsonSet = new SerializableSet();
    func.miks.forEach((value) => jsonSet.add(value));
    func.miks = jsonSet;

    return this.http
      .post<ResourceFunction>(`${this.path}/resourceType/${id}/functions`, func, {
        headers: this.getHeaders(),
      })
      .pipe(catchError(this.handleError));
  }

  updateFunction(func: ResourceFunction) {
    return this.http
      .put<ResourceFunction>(`${this.path}/functions/${func.id}`, func.content, {
        headers: this.getHeaders(),
      })
      .pipe(catchError(this.handleError));
  }

  overwriteFunctionForResource(id: number, func: ResourceFunction) {
    return this.http
      .put<ResourceFunction>(`${this.path}/resource/${id}/functions/overwrite`, func, {
        headers: this.getHeaders(),
      })
      .pipe(catchError(this.handleError));
  }

  overwriteFunctionForResourceType(id: number, func: ResourceFunction) {
    return this.http
      .put<ResourceFunction>(`${this.path}/resourceType/${id}/functions/overwrite`, func, {
        headers: this.getHeaders(),
      })
      .pipe(catchError(this.handleError));
  }

  deleteFunction(id: number) {
    return this.http
      .delete(`${this.path}/functions/${id}`, {
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
