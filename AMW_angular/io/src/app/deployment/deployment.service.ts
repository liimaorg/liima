import { Injectable } from '@angular/core';
import { Http, Response, Headers, RequestOptions, URLSearchParams } from '@angular/http';
import { Observable } from 'rxjs/Rx';
import { ComparatorFilterOption } from './comparator-filter-option';
import { Deployment } from './deployment';
import { DeploymentFilterType } from './deployment-filter-type';
import { DeploymentRequest } from './deployment-request';
import { DeploymentParameter } from './deployment-parameter';
import { DeploymentDetail } from './deployment-detail';
import * as _ from 'lodash';

@Injectable()
export class DeploymentService {
  private baseUrl: string = '/AMW_rest/resources';

  constructor(private http: Http) {
  }

  getAll(): Observable<Deployment[]> {
    return this.http
      .get(`${this.baseUrl}/deployments`, {headers: this.getHeaders()})
      .map((response: Response) => response.json())
      .catch(handleError);
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
    return this.http
      .get(`${this.baseUrl}/deployments/filter`, options)
      .map((response: Response) => this.extractDeploymentsAndTotalCount(response))
      .catch(handleError);
  }

  get(deploymentId: number): Observable<Deployment> {
    return this.http
      .get(`${this.baseUrl}/deployments/${deploymentId}`, {headers: this.getHeaders()})
      .map((response: Response) => response.json())
      .catch(handleError);
  }

  getWithActions(deploymentId: number): Observable<Deployment> {
    return this.http
      .get(`${this.baseUrl}/deployments/${deploymentId}/withActions`, {headers: this.getHeaders()})
      .map((response: Response) => response.json())
      .catch(handleError);
  }

  createDeployment(deploymentRequest: DeploymentRequest): Observable<Deployment> {
    return this.http
      .post(`${this.baseUrl}/deployments`, deploymentRequest, {headers: this.postHeaders()})
      .map((res: Response) => res.json())
      .catch(handleError);
  }

  cancelDeployment(deploymentId: number) {
    return this.http
      .put(`${this.baseUrl}/deployments/${deploymentId}/updateState`, 'canceled', {headers: this.getHeaders()})
      .map(this.extractPayload)
      .catch(handleError);
  }

  confirmDeployment(deploymentDetail: DeploymentDetail) {
    return this.http
      .put(`${this.baseUrl}/deployments/${deploymentDetail.deploymentId}/confirm`, deploymentDetail, {headers: this.getHeaders()})
      .map(this.extractPayload)
      .catch(handleError);
  }

  rejectDeployment(deploymentId: number) {
    return this.http
      .put(`${this.baseUrl}/deployments/${deploymentId}/updateState`, 'rejected', {headers: this.getHeaders()})
      .map(this.extractPayload)
      .catch(handleError);
  }

  getAllDeploymentParameterKeys(): Observable<DeploymentParameter[]> {
    return this.http
      .get(`${this.baseUrl}/deployments/deploymentParameterKeys/`, {headers: this.getHeaders()})
      .map((response: Response) => response.json())
      .catch(handleError);
  }

  canDeploy(resourceGroupId: number, contextIds: number[]): Observable<boolean> {
    const params = new URLSearchParams();
    contextIds.forEach((key) => params.append('contextId', String(key)));
    const options = new RequestOptions({
      search: params,
      headers: this.getHeaders()
    });
    return this.http
      .get(`${this.baseUrl}/deployments/canDeploy/${resourceGroupId}`, options)
      .map((response: Response) => response.json())
      .catch(handleError);
  }

  canRequestDeployment(resourceGroupId: number, contextIds: number[]): Observable<boolean> {
    const params = new URLSearchParams();
    contextIds.forEach((key) => params.append('contextId', String(key)));
    const options = new RequestOptions({
      search: params,
      headers: this.getHeaders()
    });
    return this.http
      .get(`${this.baseUrl}/deployments/canRequestDeployment/${resourceGroupId}`, options)
      .map((response: Response) => response.json())
      .catch(handleError);
  }

  canRequestDeployments(): Observable<boolean> {
    return this.http
      .get(`${this.baseUrl}/deployments/canRequestDeployment/`, {headers: this.getHeaders()})
      .map((response: Response) => response.json())
      .catch(handleError);
  }

  getAllDeploymentFilterTypes(): Observable<DeploymentFilterType[]> {
    return this.http
      .get(`${this.baseUrl}/deployments/deploymentFilterTypes/`, {headers: this.getHeaders()})
      .map((response: Response) => response.json())
      .catch(handleError);
  }

  getAllComparatorFilterOptions(): Observable<ComparatorFilterOption[]> {
    return this.http
      .get(`${this.baseUrl}/deployments/comparatorFilterOptions/`, {headers: this.getHeaders()})
      .map((response: Response) => response.json())
      .catch(handleError);
  }

  getFilterOptionValues(filterName: string): Observable<string[]> {
    const param = new URLSearchParams();
    param.append('filterName', filterName);
    const options = new RequestOptions({
      search: param,
      headers: this.getHeaders()
    });
    return this.http
      .get(`${this.baseUrl}/deployments/filterOptionValues/`, options)
      .map((response: Response) => response.json())
      .catch(handleError);
  }

  getDeploymentDetail(deploymentId: number): Observable<DeploymentDetail> {
    return this.http
      .get(`${this.baseUrl}/deployments/${deploymentId}/detail`, {headers: this.getHeaders()})
      .map((response: Response) => response.json())
      .catch(handleError);
  }

  setDeploymentDate(deploymentId: number, deploymentDate: number) {
    return this.http
      .put(`${this.baseUrl}/deployments/${deploymentId}/date`, deploymentDate, {headers: this.postHeaders()})
      .map(this.extractPayload)
      .catch(handleError);
  }

  isAngularDeploymentsGuiActive(): Observable<boolean> {
    return this.http
      .get(`${this.baseUrl}/deployments/isAngularDeploymentsGuiActive/`, {headers: this.getHeaders()})
      .map((response: Response) => response.json())
      .catch(handleError);
  }

  getCsvSeparator(): Observable<string> {
    return this.http
      .get(`${this.baseUrl}/deployments/csvSeparator/`, {headers: this.getHeaders()})
      .map((response: Response) => response.text())
      .catch(handleError);
  }

  private getHeaders() {
    const headers = new Headers();
    headers.append('Accept', 'application/json');
    return headers;
  }

  private postHeaders() {
    const headers = new Headers();
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
      errorMsg = _.escape(JSON.parse(error._body).message);
    } catch (e) {
      console.log(e);
    }
  }
  console.error(errorMsg);
  // throw an application level error
  return Observable.throw(errorMsg);
}
