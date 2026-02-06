import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, startWith, Subject } from 'rxjs';
import { map, catchError, switchMap, shareReplay } from 'rxjs/operators';
import { Resource } from '../models/resource';
import { Release } from '../models/release';
import { Relation } from '../models/relation';
import { Property } from '../models/property';
import { AppWithVersion } from '../../deployment/app-with-version';
import { BaseService } from '../../base/base.service';
import { ResourceType } from '../models/resource-type';
import { toSignal } from '@angular/core/rxjs-interop';

interface Named {
  name: string;
}

@Injectable({ providedIn: 'root' })
export class ResourceService extends BaseService {
  private http = inject(HttpClient);

  private resourceType$: Subject<ResourceType> = new Subject<ResourceType>();
  private resourceId$: Subject<number> = new Subject<number>();

  private resourceGroupListForType$: Observable<Resource[]> = this.resourceType$.pipe(
    switchMap((resourceType: ResourceType) => {
      return this.getGroupsForType(resourceType);
    }),
    startWith(null),
    shareReplay(1),
  );

  private resourceById$: Observable<Resource> = this.resourceId$.pipe(
    switchMap((id: number) => this.getResource(id)),
    shareReplay(1),
  );

  private releasesForResourceGroupByResourceId$: Observable<Release[]> = this.resourceId$.pipe(
    switchMap((resourceId: number) => {
      return this.getReleasesForResource(resourceId);
    }),
    shareReplay(1),
  );

  resourceGroupListForType = toSignal(this.resourceGroupListForType$, { initialValue: [] as Resource[] });
  releasesForResourceGroup = toSignal(this.releasesForResourceGroupByResourceId$, {
    initialValue: [] as Release[],
  });
  resource = toSignal(this.resourceById$, { initialValue: null });

  setTypeForResourceGroupList(resourcesType: ResourceType) {
    this.resourceType$.next(resourcesType);
  }

  setIdForResource(id: number) {
    this.resourceId$.next(id);
  }

  getAll(): Observable<Resource[]> {
    return this.http
      .get<Resource[]>(`${this.getBaseUrl()}/resources`, {
        headers: this.getHeaders(),
      })
      .pipe(
        map((resources) => resources.map(toResource)),
        catchError(this.handleError),
      );
  }

  getResource(resourceId: number): Observable<Resource> {
    return this.http
      .get<Resource>(`${this.getBaseUrl()}/resources/${resourceId}`, {
        headers: this.getHeaders(),
      })
      .pipe(
        map((resource) => toResource(resource)),
        catchError(this.handleError),
      );
  }

  getResourceName(resourceId: number): Observable<Named> {
    return this.getResource(resourceId).pipe(
      map((resource) => ({ name: resource.name })),
      catchError(this.handleError),
    );
  }

  resourceExists(resourceId: number): Observable<boolean> {
    return this.getResource(resourceId).pipe(
      map((resource) => !!resource),
      catchError(() => [false]),
    );
  }

  createResourceForResourceType(resource: any) {
    return this.http
      .post<Resource>(`${this.getBaseUrl()}/resources`, resource, {
        headers: this.getHeaders(),
      })
      .pipe(catchError(this.handleError));
  }

  getGroupsForType(resourceType: ResourceType): Observable<Resource[]> {
    return this.http
      .get<Resource[]>(`${this.getBaseUrl()}/resources?typeId=${resourceType.id}`, {
        headers: this.getHeaders(),
      })
      .pipe(
        map((resources) => resources.map(toResource)),
        catchError(this.handleError),
      );
  }

  get(resourceGroupName: string): Observable<Resource> {
    return this.http
      .get(`${this.getBaseUrl()}/resources/${resourceGroupName}`, {
        headers: this.getHeaders(),
      })
      .pipe(map(toResource), catchError(this.handleError));
  }

  getAllResourceGroups(): Observable<Resource[]> {
    return this.http
      .get<Resource[]>(`${this.getBaseUrl()}/resources/resourceGroups`, {
        headers: this.getHeaders(),
      })
      .pipe(
        map((resources) => resources.map(toResource)),
        catchError(this.handleError),
      );
  }

  getByType(type: string): Observable<Resource[]> {
    return this.http
      .get<Resource[]>(`${this.getBaseUrl()}/resources?type=${type}`, {
        headers: this.getHeaders(),
      })
      .pipe(
        map((resources) => resources.map(toResource)),
        catchError(this.handleError),
      );
  }

  getLatestForRelease(resourceGroupId: number, releaseId: number): Observable<Release> {
    return this.http
      .get<Release>(`${this.getBaseUrl()}/resources/resourceGroups/${resourceGroupId}/releases/${releaseId}`, {
        headers: this.getHeaders(),
      })
      .pipe(catchError(this.handleError));
  }

  getRuntime(resourceGroupName: string, releaseName: string): Observable<Relation[]> {
    const params = new HttpParams().set('type', 'RUNTIME');
    const headers = this.getHeaders();
    return this.http
      .get<Relation[]>(`${this.getBaseUrl()}/resources/${resourceGroupName}/${releaseName}/relations`, {
        params,
        headers,
      })
      .pipe(catchError(this.handleError));
  }

  getDeployableReleases(resourceGroupId: number): Observable<Release[]> {
    return this.http
      .get<Release[]>(`${this.getBaseUrl()}/resources/resourceGroups/${resourceGroupId}/releases/`, {
        headers: this.getHeaders(),
      })
      .pipe(catchError(this.handleError));
  }

  getMostRelevantRelease(resourceGroupId: number): Observable<Release> {
    return this.http
      .get<Release>(`${this.getBaseUrl()}/resources/resourceGroups/${resourceGroupId}/releases/mostRelevant/`, {
        headers: this.getHeaders(),
      })
      .pipe(catchError(this.handleError));
  }

  getAppsWithVersions(
    resourceGroupId: number,
    releaseId: number,
    environmentIds: number[],
  ): Observable<AppWithVersion[]> {
    let params = new HttpParams();
    environmentIds.forEach((id) => (params = params.append('context', String(id))));
    return this.http
      .get<AppWithVersion[]>(
        `${this.getBaseUrl()}/resources/resourceGroups/${resourceGroupId}/releases/${releaseId}/appWithVersions/`,
        {
          params: params,
          headers: this.getHeaders(),
        },
      )
      .pipe(
        map((apps) => apps.map(toAppWithVersion)),
        catchError(this.handleError),
      );
  }

  getReleasesForResource(resourceId: number): Observable<Release[]> {
    return this.http
      .get<Release[]>(`${this.getBaseUrl()}/resources/resourceGroups/releases/${resourceId}`)
      .pipe(catchError(this.handleError));
  }

  deleteResourceByResourceId(resourceId: number): Observable<void> {
    return this.http
      .delete<void>(`${this.getBaseUrl()}/resources/${resourceId}`, {
        headers: this.getHeaders(),
      })
      .pipe(catchError(this.handleError));
  }

  getAvailableReleasesForResource(resourceId: number): Observable<Release[]> {
    return this.http
      .get<Release[]>(`${this.getBaseUrl()}/resources/${resourceId}/availableReleases`, {
        headers: this.getHeaders(),
      })
      .pipe(catchError(this.handleError));
  }

  createResourceRelease(resourceGroupName: string, releaseName: string, sourceReleaseName: string): Observable<void> {
    return this.http
      .post<void>(
        `${this.getBaseUrl()}/resources/${resourceGroupName}`,
        { releaseName, sourceReleaseName },
        { headers: this.getHeaders() },
      )
      .pipe(catchError(this.handleError));
  }

  changeResourceRelease(resourceId: number, releaseId: number): Observable<void> {
    return this.http
      .put<void>(`${this.getBaseUrl()}/resources/${resourceId}/release/${releaseId}`, null, {
        headers: this.getHeaders(),
      })
      .pipe(catchError(this.handleError));
  }
}

function toAppWithVersion(r: any): AppWithVersion {
  delete r.mavenVersion;
  return r;
}

function toResource(r: Resource): Resource {
  r.defaultRelease = r.defaultRelease && toRelease(r.defaultRelease);
  r.releases = (r.releases || []).map(toRelease);
  return r;
}

function toRelease(r: any): Release {
  return { properties: [], relations: [], resourceTags: [], id: r.id, release: r.name };
}
