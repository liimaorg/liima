import { Injectable } from '@angular/core';
import { Http, Response, Headers, URLSearchParams, RequestOptions } from '@angular/http';
import { Observable } from 'rxjs/Rx';
import { Resource } from './resource';
import { Release } from './release';
import { Relation } from './relation';
import { Property } from './property';
import { AppWithVersion } from '../deployment/app-with-version';

@Injectable()
export class ResourceService {
  private baseUrl: string = 'http://localhost:8080/AMW_rest/resources';

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
    for (let i = 0; i < environmentIds.length; i++) {
      params.append('context', String(environmentIds[i]));
    }
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
  let appWithVersion = <AppWithVersion>({
    applicationId: r.applicationId,
    applicationName: r.applicationName,
    // no mavenVersion!
    version: r.version,
  });
  // console.log('Parsed appWithVersion:', appWithVersion);
  return appWithVersion;
}

function mapResources(response: Response): Resource[] {
  // uncomment to simulate error:
  // throw new Error('ups! Force choke!');
  return response.json().map(toResource);
}

function toResource(r: any): Resource {
  let resource = <Resource>({
    id: r.id,
    name: r.name,
    type: r.type,
    version: r.version,
    release: r.release ? mapRelease(r.release) : '',
    releases: r.releases ? mapReleases(r.releases) : [],
  });
  // console.log('Parsed resource:', resource);
  return resource;
}

function mapResource(response: Response): Resource {
  return toResource(response.json());
}

function mapReleases(releases): Release[] {
  return releases.map(toRelease);
}

function toRelease(r: any): Release {
  let release = <Release>({
    id: r.id,
    release: r.release,
    relations: r.relations,
    properties: r.properties,
  });
  // console.log('Parsed release:', release);
  return release;
}

function mapRelease(response: Response): Release {
  return toRelease(response.json());
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
