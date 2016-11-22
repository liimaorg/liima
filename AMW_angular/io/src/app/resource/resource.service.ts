import { Injectable } from '@angular/core';
import { Http, Response, Headers} from '@angular/http';
import { Observable } from 'rxjs/Rx';
import { Resource } from './resource';
import { Release } from './release';

@Injectable()
export class ResourceService{
  private baseUrl: string = 'http://localhost:8080/AMW_rest/resources';

  constructor(private http : Http){
  }

  getAll(): Observable<Resource[]>{
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

  getByType(type: string): Observable<Resource[]>{
    let resource$ = this.http
      .get(`${this.baseUrl}/resources?type=${type}`, {headers: this.getHeaders()})
      .map(mapResources)
      .catch(handleError);
    return resource$;
  }

  getInRelease(resourceGroupName: string, releaseName: string): Observable<Resource> {
    let resource$ = this.http
      .get(`${this.baseUrl}/resources/${resourceGroupName}/${releaseName}?env=Global`, {headers: this.getHeaders()})
      .map((response:Response) => response.json())
      .catch(handleError);
    return resource$;
  }

  private getHeaders(){
    let headers = new Headers();
    headers.append('Accept', 'application/json');
    return headers;
  }
}

function mapResources(response:Response): Resource[]{
  // uncomment to simulate error:
  // throw new Error('ups! Force choke!');
  return response.json().map(toResource);
}

function toResource(r:any): Resource{
  let resource = <Resource>({
    name: r.name,
    type: r.type,
    release: r.release ? mapRelease(r.release) : '',
    releases: r.releases ? mapReleases(r.releases) : [],
  });
  console.log('Parsed resource:', resource);
  return resource;
}

function mapResource(response:Response): Resource{
  return toResource(response.json());
}

function mapReleases(releases): Release[]{
  return releases.map(toRelease);
}

function toRelease(r:any): Release{
  let release = <Release>({
    release: r.release,
    properties: r.properties,
  });
  console.log('Parsed release:', release);
  return release;
}

function mapRelease(response:Response): Release{
  return toRelease(response.json());
}

// this could also be a private method of the component class
function handleError (error: any) {
  // log error
  // could be something more sofisticated
  let errorMsg = error.message || `Error retrieving your data`;
  console.error(errorMsg);

  // throw an application level error
  return Observable.throw(errorMsg);
}
