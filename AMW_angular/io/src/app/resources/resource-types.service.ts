import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, startWith, Subject } from 'rxjs';
import { toSignal } from '@angular/core/rxjs-interop';
import { shareReplay, switchMap } from 'rxjs/operators';
import { BaseService } from '../base/base.service';
import { ResourceType } from '../resource/resource-type';
import { ResourceTypeRequest } from '../resource/resource-type-request';

@Injectable({ providedIn: 'root' })
export class ResourceTypesService extends BaseService {
  private reload$ = new Subject<ResourceType[]>();

  private predefinedResourceTypes$ = this.getPredefinedResourceTypes();

  private rootResourceTypes$ = this.reload$.pipe(
    startWith(null),
    switchMap(() => this.getRootResourceTypes()),
    shareReplay(1),
  );

  predefinedResourceTypes = toSignal(this.predefinedResourceTypes$, { initialValue: [] as ResourceType[] });
  rootResourceTypes = toSignal(this.rootResourceTypes$, { initialValue: [] as ResourceType[] });

  constructor(private http: HttpClient) {
    super();
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
}
