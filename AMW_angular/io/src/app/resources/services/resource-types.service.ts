import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, startWith, Subject } from 'rxjs';
import { toSignal } from '@angular/core/rxjs-interop';
import { catchError, shareReplay, switchMap } from 'rxjs/operators';
import { BaseService } from '../../base/base.service';
import { ResourceType } from '../models/resource-type';
import { ResourceTypeRequest } from '../models/resource-type-request';
import { Property } from '../models/property';

@Injectable({ providedIn: 'root' })
export class ResourceTypesService extends BaseService {
  private http = inject(HttpClient);

  private reload$ = new Subject<ResourceType[]>();
  private resourceTypeId$: Subject<number> = new Subject<number>();
  private predefinedResourceTypes$ = this.getPredefinedResourceTypes();
  private propertiesContext$: Subject<{ resourceTypeId: number; contextId: number }> = new Subject<{
    resourceTypeId: number;
    contextId: number;
  }>();

  private rootResourceTypes$ = this.reload$.pipe(
    startWith(null),
    switchMap(() => this.getRootResourceTypes()),
    shareReplay(1),
  );

  private resourceTypeById$: Observable<ResourceType> = this.resourceTypeId$.pipe(
    switchMap((id: number) => this.getResourceType(id)),
    shareReplay(1),
  );

  private propertiesForResourceType$: Observable<Property[]> = this.propertiesContext$.pipe(
    switchMap(({ resourceTypeId, contextId }) => {
      return this.getProperties(resourceTypeId, contextId);
    }),
    startWith([]),
    shareReplay(1),
  );

  predefinedResourceTypes = toSignal(this.predefinedResourceTypes$, { initialValue: [] as ResourceType[] });
  rootResourceTypes = toSignal(this.rootResourceTypes$, { initialValue: [] as ResourceType[] });
  resourceType = toSignal(this.resourceTypeById$, { initialValue: null });
  properties = toSignal(this.propertiesForResourceType$, { initialValue: [] as Property[] });

  setIdForResourceType(id: number) {
    this.resourceTypeId$.next(id);
  }

  setContextForProperties(resourceTypeId: number, contextId: number) {
    this.propertiesContext$.next({ resourceTypeId, contextId });
  }

  getResourceType(id: number): Observable<ResourceType> {
    return this.http
      .get<ResourceType>(`${this.getBaseUrl()}/resourceTypes/${id}`, {
        headers: this.getHeaders(),
      })
      .pipe(catchError(this.handleError));
  }

  getAllResourceTypes(): Observable<ResourceType[]> {
    return this.http.get<ResourceType[]>(`${this.getBaseUrl()}/resourceTypes`);
  }

  getResourceTypeByName(name: string): Observable<ResourceType> {
    return this.http.get<ResourceType>(`${this.getBaseUrl()}/resourceTypes/${name}`);
  }

  getPredefinedResourceTypes(): Observable<ResourceType[]> {
    return this.http.get<ResourceType[]>(`${this.getBaseUrl()}/resourceTypes/predefinedResourceTypes`);
  }

  getRootResourceTypes(): Observable<ResourceType[]> {
    return this.http.get<ResourceType[]>(`${this.getBaseUrl()}/resourceTypes/rootResourceTypes`);
  }

  addNewResourceType(resourceTypeRequest: ResourceTypeRequest): Observable<void> {
    return this.http.post<void>(`${this.getBaseUrl()}/resourceTypes`, resourceTypeRequest);
  }

  getProperties(resourceTypeId: number, contextId: number = 1): Observable<Property[]> {
    const params = new HttpParams().set('contextId', contextId.toString());
    return this.http
      .get<Property[]>(`${this.getBaseUrl()}/resources/resourceType/${resourceTypeId}/properties`, {
        params,
        headers: this.getHeaders(),
      })
      .pipe(catchError(this.handleError));
  }

  refreshData() {
    this.reload$.next([]);
  }

  delete(id: number): Observable<number> {
    return this.http
      .delete<number>(`${this.getBaseUrl()}/resourceTypes/${id}`, {
        headers: this.getHeaders(),
      })
      .pipe(catchError(this.handleError));
  }
}
