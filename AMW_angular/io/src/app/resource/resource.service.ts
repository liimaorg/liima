import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, of, startWith, Subject } from 'rxjs';
import { map, catchError, switchMap, shareReplay } from 'rxjs/operators';
import { Resource } from './resource';
import { Release } from './release';
import { Relation } from './relation';
import { Property } from './property';
import { AppWithVersion } from '../deployment/app-with-version';
import { BaseService } from '../base/base.service';
import { ResourceType } from './resource-type';
import { toSignal } from '@angular/core/rxjs-interop';

interface Named {
  name: string;
}

@Injectable({ providedIn: 'root' })
export class ResourceService extends BaseService {
  private resourceType$: Subject<ResourceType> = new Subject<ResourceType>();

  private resourceGroupListForType: Observable<Resource[]> = this.resourceType$.pipe(
    startWith(null),
    switchMap((resourceType: ResourceType) => this.getGroupsForType(resourceType)),
    shareReplay(1),
  );

  resourceGroupListForTypeSignal = toSignal(this.resourceGroupListForType, { initialValue: [] as Resource[] });

  constructor(private http: HttpClient) {
    super();
  }

  setTypeForResourceGroupList(resourcesType: ResourceType) {
    this.resourceType$.next(resourcesType);
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

  getResourceName(resourceId: number): Observable<Named> {
    return this.http
      .get<Named>(`${this.getBaseUrl()}/resources/name/${resourceId}`, {
        headers: this.getHeaders(),
      })
      .pipe(catchError(this.handleError));
  }

  getGroupsForType(resourceType: ResourceType): Observable<Resource[]> {
    console.log(resourceType);
    console.log(resourceType.name);
    /*    return this.http
      .get<Resource[]>(`${this.getBaseUrl()}/resources?&type=${resourceTypeName}`, {
        headers: this.getHeaders(),
      })
      .pipe(
        map((resources) => resources.map(toResource)),
        catchError(this.handleError),
      );*/
    return of([]);
  }
  get(resourceGroupName: string): Observable<Resource> {
    return this.http
      .get(`${this.getBaseUrl()}/resources/${resourceGroupName}`, {
        headers: this.getHeaders(),
      })
      .pipe(map(toResource), catchError(this.handleError));
  }

  resourceExists(resourceId: number): Observable<Resource> {
    return this.http
      .get<Resource>(`${this.getBaseUrl()}/resources/exists/${resourceId}`, {
        headers: this.getHeaders(),
      })
      .pipe(catchError(this.handleError));
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

  getProperty(resourceGroupName: string, releaseName: string, propertyName: string): Observable<Property> {
    return this.http
      .get<Property>(`${this.getBaseUrl()}/resources/${resourceGroupName}/${releaseName}/properties/${propertyName}`, {
        headers: this.getHeaders(),
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
}

function toAppWithVersion(r: any): AppWithVersion {
  delete r.mavenVersion;
  return r;
}

function toResource(r: Resource): Resource {
  r.release = r.release && toRelease(r.release);
  r.releases = (r.releases || []).map(toRelease);
  return r;
}

function toRelease(r: any): Release {
  return { properties: [], relations: [], resourceTags: [], id: r.id, release: r.name };
}
