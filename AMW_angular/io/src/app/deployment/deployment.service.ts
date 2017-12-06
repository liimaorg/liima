import { Injectable } from '@angular/core';
import { Http, Response, Headers, RequestOptions, URLSearchParams } from '@angular/http';
import { Observable } from 'rxjs/Rx';
import { ComparatorFilterOption } from './comparator-filter-option';
import { Deployment } from './deployment';
import { DeploymentFilterType } from './deployment-filter-type';
import { DeploymentRequest } from './deployment-request';
import { DeploymentParameter } from './deployment-parameter';
import { DeploymentDetail } from './deployment-detail';

@Injectable()
export class DeploymentService {
  private baseUrl: string = '/AMW_rest/resources';

  constructor(private http: Http) {
  }

  getAll(): Observable<Deployment[]> {
    const resource$ = this.http
      .get(`${this.baseUrl}/deployments`, {headers: this.getHeaders()})
      .map((response: Response) => response.json())
      .catch(handleError);
    return resource$;
  }

  getFilteredDeployments(filterString: string, sortCol: string, sortDir: string, offset: number, maxResults: number): Observable<{ deployments: Deployment[], total: number }> {
    const params = new URLSearchParams();
    params.append('filters', filterString);
    params.append('colToSort', sortCol);
    params.append('sortDirection', sortDir);
    params.append('offset', String(offset));
    params.append('maxResults', String(maxResults));
    const options = new RequestOptions({
      search: params,
      headers: this.getHeaders()
    });
    const resource$ = this.http
      .get(`${this.baseUrl}/deployments/filter`, options)
      .map((response: Response) => this.extractDeploymentsAndTotalCount(response))
      .catch(handleError);
    return resource$;
  }

  get(deploymentId: number): Observable<Deployment> {
    const resource$ = this.http
      .get(`${this.baseUrl}/deployments/${deploymentId}`, {headers: this.getHeaders()})
      .map((response: Response) => response.json())
      .catch(handleError);
    return resource$;
  }

  getWithActions(deploymentId: number): Observable<Deployment> {
    const resource$ = this.http
      .get(`${this.baseUrl}/deployments/${deploymentId}/withActions`, {headers: this.getHeaders()})
      .map((response: Response) => response.json())
      .catch(handleError);
    return resource$;
  }

  createDeployment(deploymentRequest: DeploymentRequest): Observable<Deployment> {
    return this.http
      .post(`${this.baseUrl}/deployments`, deploymentRequest, {headers: this.postHeaders()})
      .map((res: Response) => res.json())
      .catch((error: any) => Observable.throw(error.json().error || 'Server error'));
  }

  cancelDeployment(deploymentId: number) {
    const resource$ = this.http
      .put(`${this.baseUrl}/deployments/${deploymentId}/updateState`, 'canceled', {headers: this.getHeaders()})
      .map(this.extractPayload)
      .catch(handleError);
    return resource$;
  }

  confirmDeployment(deploymentDetail: DeploymentDetail) {
    const resource$ = this.http
      .put(`${this.baseUrl}/deployments/${deploymentDetail.deploymentId}/confirm`, deploymentDetail, {headers: this.getHeaders()})
      .map(this.extractPayload)
      .catch(handleError);
    return resource$;
  }

  rejectDeployment(deploymentId: number) {
    const resource$ = this.http
      .put(`${this.baseUrl}/deployments/${deploymentId}/updateState`, 'rejected', {headers: this.getHeaders()})
      .map(this.extractPayload)
      .catch(handleError);
    return resource$;
  }

  getAllDeploymentParameterKeys(): Observable<DeploymentParameter[]> {
    const resource$ = this.http
      .get(`${this.baseUrl}/deployments/deploymentParameterKeys/`, {headers: this.getHeaders()})
      .map((response: Response) => response.json())
      .catch(handleError);
    return resource$;
  }

  canDeploy(resourceGroupId: number, contextIds: number[]): Observable<boolean> {
    const params = new URLSearchParams();
    contextIds.forEach((key) => params.append('contextId', String(key)));
    const options = new RequestOptions({
      search: params,
      headers: this.getHeaders()
    });
    const resource$ = this.http
      .get(`${this.baseUrl}/deployments/canDeploy/${resourceGroupId}`, options)
      .map((response: Response) => response.json())
      .catch(handleError);
    return resource$;
  }

  canRequestDeployment(resourceGroupId: number, contextIds: number[]): Observable<boolean> {
    const params = new URLSearchParams();
    contextIds.forEach((key) => params.append('contextId', String(key)));
    const options = new RequestOptions({
      search: params,
      headers: this.getHeaders()
    });
    const resource$ = this.http
      .get(`${this.baseUrl}/deployments/canRequestDeployment/${resourceGroupId}`, options)
      .map((response: Response) => response.json())
      .catch(handleError);
    return resource$;
  }

  canRequestDeployments(): Observable<boolean> {
    const resource$ = this.http
      .get(`${this.baseUrl}/deployments/canRequestDeployment/`, {headers: this.getHeaders()})
      .map((response: Response) => response.json())
      .catch(handleError);
    return resource$;
  }

  getAllDeploymentFilterTypes(): Observable<DeploymentFilterType[]> {
    const resource$ = this.http
      .get(`${this.baseUrl}/deployments/deploymentFilterTypes/`, {headers: this.getHeaders()})
      .map((response: Response) => response.json())
      .catch(handleError);
    return resource$;
  }

  getAllComparatorFilterOptions(): Observable<ComparatorFilterOption[]> {
    const resource$ = this.http
      .get(`${this.baseUrl}/deployments/comparatorFilterOptions/`, {headers: this.getHeaders()})
      .map((response: Response) => response.json())
      .catch(handleError);
    return resource$;
  }

  getFilterOptionValues(filterName: string): Observable<string[]> {
    const param = new URLSearchParams();
    param.append('filterName', filterName);
    const options = new RequestOptions({
      search: param,
      headers: this.getHeaders()
    });
    const resource$ = this.http
      .get(`${this.baseUrl}/deployments/filterOptionValues/`, options)
      .map((response: Response) => response.json())
      .catch(handleError);
    return resource$;
  }

  getDeploymentDetail(deploymentId: number): Observable<DeploymentDetail> {
    const resource$ = this.http
      .get(`${this.baseUrl}/deployments/${deploymentId}/detail`, {headers: this.getHeaders()})
      .map((response: Response) => response.json())
      .catch(handleError);
    return resource$;
  }

  setDeploymentDate(deploymentId: number, deploymentDate: number) {
    const resource$ = this.http
      .put(`${this.baseUrl}/deployments/${deploymentId}/date`, deploymentDate, {headers: this.postHeaders()})
      .map(this.extractPayload)
      .catch(handleError);
    return resource$;
  }

  isAngularDeploymentsGuiActive(): Observable<boolean> {
    const resource$ = this.http
      .get(`${this.baseUrl}/deployments/isAngularDeploymentsGuiActive/`, {headers: this.getHeaders()})
      .map((response: Response) => response.json())
      .catch(handleError);
    return resource$;
  }

  getCsvSeparator(): Observable<string> {
    const resource$ = this.http
      .get(`${this.baseUrl}/deployments/csvSeparator/`, {headers: this.getHeaders()})
      .map((response: Response) => response.text())
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

  // to json without throwing an error if response is empty
  private extractPayload(res: Response) {
    return res.text() ? res.json() : {};
  }

  private extractDeploymentsAndTotalCount(res: Response) {
    const headerField: string = 'X-Total-Count';
    const ob: { deployments: Deployment[], total: number } = { deployments: [], total: 0 };
    ob.deployments = this.extractPayload(res);
    ob.total = res.headers.get(headerField) ? parseInt(res.headers.get(headerField), 10) : 0;
    return ob;
  }
}

// this could also be a private method of the component class
function handleError(error: any) {
  let errorMsg = 'Error retrieving your data';
  if (error._body) {
    try {
      errorMsg = JSON.parse(error._body).message;
    } catch (e) {
      console.log(e);
    }
  }
  console.error(errorMsg);
  // throw an application level error
  return Observable.throw(errorMsg);
}
