import { Injectable } from '@angular/core';
import { Http, Response, URLSearchParams, RequestOptions } from '@angular/http';
import { Observable } from 'rxjs/Rx';
import { Resource } from './resource';
import { ResourceType } from './resource-type';
import { Release } from './release';
import { Relation } from './relation';
import { Property } from './property';
import { AppWithVersion } from '../deployment/app-with-version';
import { BaseService } from '../base/base.service';

@Injectable()
export class ResourceService extends BaseService {

  constructor(private http: Http) {
    super();
  }

  getAll(): Observable<Resource[]> {
    return this.http
      .get(`${this.getBaseUrl()}/resources`, {headers: this.getHeaders()})
      .map(mapResources)
      .catch(this.handleError);
  }

  getResourceName(resourceId: number): Observable<string> {
    return this.http
      .get(`${this.getBaseUrl()}/resources/name/${resourceId}`, {headers: this.getHeaders()})
      .map((response: Response) => response.text());
  }

  get(resourceGroupName: string): Observable<Resource> {
    return this.http
      .get(`${this.getBaseUrl()}/resources/${resourceGroupName}`, {headers: this.getHeaders()})
      .map(mapResource);
  }

  resourceExists(resourceId: number): Observable<Resource> {
    return this.http
      .get(`${this.getBaseUrl()}/resources/exists/${resourceId}`, {headers: this.getHeaders()})
      .map((response: Response) => response.json())
      .catch(this.handleError);
  }

  getAllResourceGroups(): Observable<Resource[]> {
    return this.http
      .get(`${this.getBaseUrl()}/resources/resourceGroups`, {headers: this.getHeaders()})
      .map(mapResources)
      .catch(this.handleError);
  }

  getAllResourceTypes(): Observable<ResourceType[]> {
    return this.http
      .get(`${this.getBaseUrl()}/resources/resourceTypes`, {headers: this.getHeaders()})
      .map(mapResources)
      .catch(this.handleError);
  }

  getByType(type: string): Observable<Resource[]> {
    return this.http
      .get(`${this.getBaseUrl()}/resources?type=${type}`, {headers: this.getHeaders()})
      .map(mapResources)
      .catch(this.handleError);
  }

  getLatestForRelease(resourceGroupId: number, releaseId: number): Observable<Release> {
    return this.http
      .get(`${this.getBaseUrl()}/resources/resourceGroups/${resourceGroupId}/releases/${releaseId}`, {headers: this.getHeaders()})
      .map((response: Response) => response.json())
      .catch(this.handleError);
  }

  getRuntime(resourceGroupName: string, releaseName: string): Observable<Relation[]> {
    const params: URLSearchParams = new URLSearchParams();
    params.set('type', 'RUNTIME');
    const options = new RequestOptions({search: params, headers: this.getHeaders()});
    return this.http
      .get(`${this.getBaseUrl()}/resources/${resourceGroupName}/${releaseName}/relations`, options)
      .map((response: Response) => response.json())
      .catch(this.handleError);
  }

  getProperty(resourceGroupName: string, releaseName: string, propertyName: string): Observable<Property> {
    return this.http
      .get(`${this.getBaseUrl()}/resources/${resourceGroupName}/${releaseName}/properties/${propertyName}`, {headers: this.getHeaders()})
      .map((response: Response) => response.json())
      .catch(this.handleError);
  }

  getDeployableReleases(resourceGroupId: number): Observable<Release[]> {
    return this.http
      .get(`${this.getBaseUrl()}/resources/resourceGroups/${resourceGroupId}/releases/`, {headers: this.getHeaders()})
      .map((response: Response) => response.json())
      .catch(this.handleError);
  }

  getMostRelevantRelease(resourceGroupId: number): Observable<Release> {
    return this.http
      .get(`${this.getBaseUrl()}/resources/resourceGroups/${resourceGroupId}/releases/mostRelevant/`, {headers: this.getHeaders()})
      .map((response: Response) => response.json())
      .catch(this.handleError);
  }

  getAppsWithVersions(resourceGroupId: number, releaseId: number, environmentIds: number[]): Observable<AppWithVersion[]> {
    const params: URLSearchParams = new URLSearchParams();
    environmentIds.forEach((id) => params.append('context', String(id)));
    const options = new RequestOptions({search: params, headers: this.getHeaders()});
    return this.http
      .get(`${this.getBaseUrl()}/resources/resourceGroups/${resourceGroupId}/releases/${releaseId}/appWithVersions/`, options)
      .map(mapAppWithVersion)
      .catch(this.handleError);
  }

  canCreateShakedownTest(resourceGroupId: number): Observable<boolean> {
    return this.http
      .get(`${this.getBaseUrl()}/resources/resourceGroups/${resourceGroupId}/canCreateShakedownTest`, {headers: this.getHeaders()})
      .map((response: Response) => response.json())
      .catch(this.handleError);
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

