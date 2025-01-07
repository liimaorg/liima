import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, startWith, Subject } from 'rxjs';
import { toSignal } from '@angular/core/rxjs-interop';
import { catchError, shareReplay, switchMap } from 'rxjs/operators';
import { BaseService } from '../../base/base.service';
import { ResourceType } from '../models/resource-type';
import { ResourceTypeRequest } from '../models/resource-type-request';

@Injectable({ providedIn: 'root' })
export class ResourceTypesService extends BaseService {
  private reload$ = new Subject<ResourceType[]>();
  private resourceTypeId$: Subject<Number> = new Subject<Number>();
  private predefinedResourceTypes$ = this.getPredefinedResourceTypes();

  private rootResourceTypes$ = this.reload$.pipe(
    startWith(null),
    switchMap(() => this.getRootResourceTypes()),
    shareReplay(1),
  );

  private resourceTypeById$: Observable<ResourceType> = this.resourceTypeId$.pipe(
    switchMap((id: number) => this.getResourceType(id)),
    shareReplay(1),
  );

  predefinedResourceTypes = toSignal(this.predefinedResourceTypes$, { initialValue: [] as ResourceType[] });
  rootResourceTypes = toSignal(this.rootResourceTypes$, { initialValue: [] as ResourceType[] });
  resourceType = toSignal(this.resourceTypeById$, { initialValue: null });

  constructor(private http: HttpClient) {
    super();
  }

  setIdForResourceType(id: number) {
    this.resourceTypeId$.next(id);
  }

  getResourceType(id: number): Observable<ResourceType> {
    return this.http
      .get<ResourceType>(`${this.getBaseUrl()}/resources/resourceTypes/${id}`, {
        headers: this.getHeaders(),
      })
      .pipe(catchError(this.handleError));
  }

  getAllResourceTypes(): Observable<ResourceType[]> {
    return this.http.get<ResourceType[]>(`${this.getBaseUrl()}/resources/resourceTypes`);
  }

  getPredefinedResourceTypes(): Observable<ResourceType[]> {
    return this.http.get<ResourceType[]>(`${this.getBaseUrl()}/resources/predefinedResourceTypes`);
  }

  getRootResourceTypes(): Observable<ResourceType[]> {
    return this.http.get<ResourceType[]>(`${this.getBaseUrl()}/resources/rootResourceTypes`);
  }

  addNewResourceType(resourceTypeRequest: ResourceTypeRequest): Observable<void> {
    return this.http.post<void>(`${this.getBaseUrl()}/resources/resourceTypes`, resourceTypeRequest);
  }

  refreshData() {
    this.reload$.next([]);
  }

  delete(id: number): Observable<number> {
    return this.http
      .delete<number>(`${this.getBaseUrl()}/resources/resourceTypes/${id}`, {
        headers: this.getHeaders(),
      })
      .pipe(catchError(this.handleError));
  }
}
