import { Injectable } from '@angular/core';
import { Http, Response, Headers, RequestOptions, URLSearchParams } from '@angular/http';
import { Observable } from 'rxjs/Rx';
import { ComparatorFilterOption } from './comparator-filter-option';
import { Deployment } from './deployment';
import { DeploymentFilterType } from './deployment-filter-type';
import { DeploymentRequest } from './deployment-request';
import { DeploymentParameter } from './deployment-parameter';

@Injectable()
export class DeploymentService {
  private baseUrl: string = '/AMW_rest/resources';

  constructor(private http: Http) {
  }

  getAll(): Observable<Deployment[]> {
    let resource$ = this.http
      .get(`${this.baseUrl}/deployments`, {headers: this.getHeaders()})
      .map((response: Response) => response.json())
      .catch(handleError);
    return resource$;
  }

  getFilteredDeployments(filterString: string): Observable<Deployment[]> {
    let param = new URLSearchParams();
    param.append('filters', filterString);
    let options = new RequestOptions({
      search: param,
      headers: this.getHeaders()
    });
    let resource$ = this.http
      .get(`${this.baseUrl}/deployments/filter`, options)
      .map((response: Response) => response.json())
      .catch(handleError);
    return resource$;
  }

  get(deploymentId: number): Observable<Deployment> {
    let resource$ = this.http
      .get(`${this.baseUrl}/deployments/${deploymentId}`, {headers: this.getHeaders()})
      .map((response: Response) => response.json())
      .catch(handleError);
    return resource$;
  }

  createDeployment(deploymentRequest: DeploymentRequest): Observable<Deployment> {
    return this.http.post(`${this.baseUrl}/deployments`, deploymentRequest, {headers: this.postHeaders()}).map((res: Response) => res.json())
      .catch((error: any) => Observable.throw(error.json().error || 'Server error'));
  }

  getAllDeploymentParameterKeys(): Observable<DeploymentParameter[]> {
    let resource$ = this.http
      .get(`${this.baseUrl}/deployments/deploymentParameterKeys/`, {headers: this.getHeaders()})
      .map((response: Response) => response.json())
      .catch(handleError);
    return resource$;
  }

  canDeploy(resourceGroupId: number, contextIds: number[]): Observable<boolean> {
    let params = new URLSearchParams();
    contextIds.forEach((key) => params.append('contextId', String(key)));
    let options = new RequestOptions({
      search: params,
      headers: this.getHeaders()
    });
    let resource$ = this.http
      .get(`${this.baseUrl}/deployments/canDeploy/${resourceGroupId}`, options)
      .map((response: Response) => response.json())
      .catch(handleError);
    return resource$;
  }

  canRequestDeployment(resourceGroupId: number, contextIds: number[]): Observable<boolean> {
    let params = new URLSearchParams();
    contextIds.forEach((key) => params.append('contextId', String(key)));
    let options = new RequestOptions({
      search: params,
      headers: this.getHeaders()
    });
    let resource$ = this.http
      .get(`${this.baseUrl}/deployments/canRequestDeployment/${resourceGroupId}`, options)
      .map((response: Response) => response.json())
      .catch(handleError);
    return resource$;
  }

  getAllDeploymentFilterTypes(): Observable<DeploymentFilterType[]> {
    let resource$ = this.http
      .get(`${this.baseUrl}/deployments/deploymentFilterTypes/`, {headers: this.getHeaders()})
      .map((response: Response) => response.json())
      .catch(handleError);
    return resource$;
  }

  getAllComparatorFilterOptions(): Observable<ComparatorFilterOption[]> {
    let resource$ = this.http
      .get(`${this.baseUrl}/deployments/comparatorFilterOptions/`, {headers: this.getHeaders()})
      .map((response: Response) => response.json())
      .catch(handleError);
    return resource$;
  }

  getFilterOptionValues(filterName: string): Observable<string[]> {
    let param = new URLSearchParams();
    param.append('filterName', filterName);
    let options = new RequestOptions({
      search: param,
      headers: this.getHeaders()
    });
    let resource$ = this.http
      .get(`${this.baseUrl}/deployments/filterOptionValues/`, options)
      .map((response: Response) => response.json())
      .catch(handleError);
    return resource$;
  }

  private getHeaders() {
    let headers = new Headers();
    headers.append('Accept', 'application/json');
    return headers;
  }

  private postHeaders() {
    let headers = new Headers();
    headers.append('Content-Type', 'application/json');
    headers.append('Accept', 'application/json');
    return headers;
  }
}

// this could also be a private method of the component class
function handleError(error: any) {
  // log error
  // could be something more sofisticated
  let errorMsg = error.message || `Error retrieving your data`;
  console.error(errorMsg);

  // throw an application level error
  return Observable.throw(errorMsg);
}
