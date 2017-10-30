import { Injectable } from '@angular/core';
import { Http, Response, Headers, URLSearchParams, RequestOptions } from '@angular/http';
import { Observable } from 'rxjs/Rx';
import { Resource } from './resource';
import { ResourceType } from './resource-type';
import { Release } from './release';
import { Relation } from './relation';
import { Property } from './property';
import { AppWithVersion } from '../deployment/app-with-version';

@Injectable()
export class ResourceService {
  private baseUrl: string = '/AMW_rest/resources';

  constructor(private http: Http) {
  }

  getAll(): Observable<Resource[]> {
    let resource$ = this.http
      .get(`${this.baseUrl}/resources`, {headers: this.getHeaders()})
      .map(mapResources)
      .catch(handleError);
    return resource$;
  }

  get(resourceGroupName: string): Observable<Resource> {
    let resource$ = this.http
      .get(`${this.baseUrl}/resources/${resourceGroupName}`, {headers: this.getHeaders()})
      .map(mapResource);
    return resource$;
  }

  getAllResourceGroups(): Observable<Resource[]> {
    let resource$ = this.http
      .get(`${this.baseUrl}/resources/resourceGroups`, {headers: this.getHeaders()})
      .map(mapResources)
      .catch(handleError);
    return resource$;
  }

  getAllResourceTypes(): Observable<ResourceType[]> {
    let resource$ = this.http
      .get(`${this.baseUrl}/resources/resourceTypes`, {headers: this.getHeaders()})
      .map(mapResources)
      .catch(handleError);
    return resource$;
  }

  getByType(type: string): Observable<Resource[]> {
    let resource$ = this.http
      .get(`${this.baseUrl}/resources?type=${type}`, {headers: this.getHeaders()})
      .map(mapResources)
      .catch(handleError);
    return resource$;
  }

  getLatestForRelease(resourceGroupId: number, releaseId: number): Observable<Release> {
    let resource$ = this.http
      .get(`${this.baseUrl}/resources/resourceGroups/${resourceGroupId}/releases/${releaseId}`, {headers: this.getHeaders()})
      .map((response: Response) => response.json())
      .catch(handleError);
    return resource$;
  }

  getRuntime(resourceGroupName: string, releaseName: string): Observable<Relation[]> {
    let params: URLSearchParams = new URLSearchParams();
    params.set('type', 'RUNTIME');
    let options = new RequestOptions({
      search: params,
      headers: this.getHeaders()
    });
    let resource$ = this.http
      .get(`${this.baseUrl}/resources/${resourceGroupName}/${releaseName}/relations`, options)
      .map((response: Response) => response.json())
      .catch(handleError);
    return resource$;
  }

  getProperty(resourceGroupName: string, releaseName: string, propertyName: string): Observable<Property> {
    let resource$ = this.http
      .get(`${this.baseUrl}/resources/${resourceGroupName}/${releaseName}/properties/${propertyName}`, {headers: this.getHeaders()})
      .map((response: Response) => response.json())
      .catch(handleError);
    return resource$;
  }

  getDeployableReleases(resourceGroupId: number): Observable<Release[]> {
    let resource$ = this.http
      .get(`${this.baseUrl}/resources/resourceGroups/${resourceGroupId}/releases/`, {headers: this.getHeaders()})
      .map((response: Response) => response.json())
      .catch(handleError);
    return resource$;
  }

  getMostRelevantRelease(resourceGroupId: number): Observable<Release> {
    let resource$ = this.http
      .get(`${this.baseUrl}/resources/resourceGroups/${resourceGroupId}/releases/mostRelevant/`, {headers: this.getHeaders()})
      .map((response: Response) => response.json())
      .catch(handleError);
    return resource$;
  }

  getAppsWithVersions(resourceGroupId: number, releaseId: number, environmentIds: number[]): Observable<AppWithVersion[]> {
    let params: URLSearchParams = new URLSearchParams();
    environmentIds.forEach((id) => params.append('context', String(id)));
    let options = new RequestOptions({
      search: params,
      headers: this.getHeaders()
    });
    let resource$ = this.http
      .get(`${this.baseUrl}/resources/resourceGroups/${resourceGroupId}/releases/${releaseId}/appWithVersions/`, options)
      .map(mapAppWithVersion)
      .catch(handleError);
    return resource$;
  }

  canCreateShakedownTest(resourceGroupId: number): Observable<boolean> {
    let resource$ = this.http
      .get(`${this.baseUrl}/resources/resourceGroups/${resourceGroupId}/canCreateShakedownTest`, {headers: this.getHeaders()})
      .map((response: Response) => response.json())
      .catch(handleError);
    return resource$;
  }

  private getHeaders() {
    let headers = new Headers();
    headers.append('Accept', 'application/json');
    return headers;
  }
}

function mapAppWithVersion(response: Response): AppWithVersion[] {
  // uncomment to simulate error:
  // throw new Error('ups! Force choke!');
  return response.json().map(toAppWithVersion);
}

function toAppWithVersion(r: any): AppWithVersion {
  delete r.mavenVersion;
  return r;
}

function mapResources(response: Response): Resource[] {
  // uncomment to simulate error:
  // throw new Error('ups! Force choke!');
  return response.json().map(toResource);
}

function toResource(r: any): Resource {
  r.release = r.release && toRelease(r.release);
  r.releases = (r.releases || []).map(toRelease);
  return r;
}

function mapResource(response: Response): Resource {
  return toResource(response.json());
}

function toRelease(r: any): Release {
  delete r.templates;
  return r;
}

// this could also be a private method of the component class
function handleError(error: any) {
  // log error
  // could be something more sophisticated
  let errorMsg = error.message || `Error retrieving your data`;
  console.error(errorMsg);

  // throw an application level error
  return Observable.throw(errorMsg);
}
