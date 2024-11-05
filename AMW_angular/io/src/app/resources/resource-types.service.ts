import { Injectable } from '@angular/core';

import { HttpClient } from '@angular/common/http';
import { Observable, startWith, Subject } from 'rxjs';
import { toSignal } from '@angular/core/rxjs-interop';
import { shareReplay, switchMap } from 'rxjs/operators';
import { ResourceType } from './resourceType';
import { BaseService } from '../base/base.service';

@Injectable({ providedIn: 'root' })
export class ResourceTypesService extends BaseService {
  private reload$ = new Subject<ResourceType[]>();

  private predefinedResources$ = this.getPredefinedResourceTypes();

  private rootResources$ = this.reload$.pipe(
    startWith(null),
    switchMap(() => this.getRootResourceTypes()),
    shareReplay(1),
  );

  predefinedResources = toSignal(this.predefinedResources$, { initialValue: [] as ResourceType[] });
  rootResources = toSignal(this.rootResources$, { initialValue: [] as ResourceType[] });

  constructor(private http: HttpClient) {
    super();
  }

  getPredefinedResourceTypes(): Observable<ResourceType[]> {
    return this.http.get<ResourceType[]>(`${this.getBaseUrl()}/resources/predefinedResourceTypes`);
  }

  getRootResourceTypes(): Observable<ResourceType[]> {
    return this.http.get<ResourceType[]>(`${this.getBaseUrl()}/resources/rootResourceTypes`);
  }

  refreshData() {
    this.reload$.next([]);
  }
}
