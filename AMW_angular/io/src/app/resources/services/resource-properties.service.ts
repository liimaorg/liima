import { inject, Injectable } from '@angular/core';
import { BaseService } from '../../base/base.service';
import { HttpClient, HttpParams } from '@angular/common/http';
import { toSignal } from '@angular/core/rxjs-interop';
import { Observable, startWith, Subject } from 'rxjs';
import { Property } from '../models/property';
import { catchError, shareReplay, switchMap } from 'rxjs/operators';

export interface PropertyUpdate {
  name: string;
  value: string | null;
}

@Injectable({ providedIn: 'root' })
export class ResourcePropertiesService extends BaseService {
  private http = inject(HttpClient);

  private path = `${this.getBaseUrl()}`;
  private properties$: Subject<{ id: number; contextId: number }> = new Subject<{
    id: number;
    contextId: number;
  }>();
  private propertiesForType$: Subject<{ id: number; contextId: number }> = new Subject<{
    id: number;
    contextId: number;
  }>();

  private propertiesForResource$: Observable<Property[]> = this.properties$.pipe(
    switchMap(({ id, contextId }) => {
      return this.getResourceProperties(id, contextId);
    }),
    startWith([]),
    shareReplay(1),
  );

  private propertiesForTypeResource$: Observable<Property[]> = this.propertiesForType$.pipe(
    switchMap(({ id, contextId }) => {
      return this.getResourceTypeProperties(id, contextId);
    }),
    startWith([]),
    shareReplay(1),
  );

  properties = toSignal(this.propertiesForResource$, { initialValue: [] as Property[] });
  propertiesForType = toSignal(this.propertiesForTypeResource$, { initialValue: [] as Property[] });

  setIdsForResourceProperties(id: number, contextId: number) {
    this.properties$.next({ id, contextId });
  }

  setIdsForResourceTypeProperties(id: number, contextId: number) {
    this.propertiesForType$.next({ id, contextId });
  }

  getResourceProperties(id: number, contextId: number = 1): Observable<Property[]> {
    const params = new HttpParams().set('contextId', contextId.toString());
    return this.http
      .get<Property[]>(`${this.getBaseUrl()}/resources/${id}/properties`, {
        params,
        headers: this.getHeaders(),
      })
      .pipe(catchError(this.handleError));
  }

  getResourceTypeProperties(id: number, contextId: number = 1): Observable<Property[]> {
    const params = new HttpParams().set('contextId', contextId.toString());
    return this.http
      .get<Property[]>(`${this.getBaseUrl()}/resourceTypes/${id}/properties`, {
        params,
        headers: this.getHeaders(),
      })
      .pipe(catchError(this.handleError));
  }

  bulkUpdateResourcePropertiesValues(
    id: number,
    updates: PropertyUpdate[],
    resets: PropertyUpdate[],
    contextId: number = 1,
  ): Observable<void> {
    const params = new HttpParams().set('contextId', contextId.toString());
    const properties = { updates: updates, resets: resets };
    return this.http
      .put<void>(`${this.getBaseUrl()}/resources/${id}/properties`, properties, {
        params,
        headers: this.getHeaders(),
      })
      .pipe(catchError(this.handleError));
  }
}
