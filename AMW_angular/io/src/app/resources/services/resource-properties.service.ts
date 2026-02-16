import { inject, Injectable } from '@angular/core';
import { BaseService } from '../../base/base.service';
import { HttpClient, HttpParams } from '@angular/common/http';
import { toSignal } from '@angular/core/rxjs-interop';
import { Observable, startWith, Subject } from 'rxjs';
import { Property } from '../models/property';
import { catchError, shareReplay, switchMap } from 'rxjs/operators';
import { PropertyDiff } from '../models/property-diff';

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
  private propertyOverwriteInfo$: Subject<{ resourceId: number; contextId: number; propertyName: string }> =
    new Subject<{
      resourceId: number;
      contextId: number;
      propertyName: string;
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

  private overwriteInfoForProperty$: Observable<PropertyDiff[]> = this.propertyOverwriteInfo$.pipe(
    switchMap(({ resourceId, contextId, propertyName }) => {
      return this.getOverwriteInfo(resourceId, contextId, propertyName);
    }),
    startWith([]),
    shareReplay(1),
  );

  properties = toSignal(this.propertiesForResource$, { initialValue: [] as Property[] });
  propertiesForType = toSignal(this.propertiesForTypeResource$, { initialValue: [] as Property[] });
  overwriteInfoForProperty = toSignal(this.overwriteInfoForProperty$, { initialValue: [] as PropertyDiff[] });

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

  getOverwriteInfo(resourceId: number, contextId: number, propertyName: string): Observable<PropertyDiff[]> {
    const params = new HttpParams().set('contextId', contextId.toString());
    return this.http
      .get<PropertyDiff[]>(
        `${this.getBaseUrl()}/resourceTypes/${resourceId}/properties/${propertyName}/overwriteInfo`,
        {
          params,
          headers: this.getHeaders(),
        },
      )
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
