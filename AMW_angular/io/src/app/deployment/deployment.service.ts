import { Injectable } from '@angular/core';
import { Http, Response, Headers, RequestOptions, URLSearchParams } from '@angular/http';
import { Observable } from 'rxjs/Rx';
import { ComparatorFilterOption } from './comparator-filter-option';
import { Deployment } from './deployment';
import { DeploymentFilterType } from './deployment-filter-type';
import { DeploymentRequest } from './deployment-request';
import { DeploymentParameter } from './deployment-parameter';
import { BaseService } from '../base/base.service';

@Injectable()
export class DeploymentService extends BaseService {

  constructor(private http: Http) {
    super();
  }

  getAll(): Observable<Deployment[]> {
    return this.http
      .get(`${this.getBaseUrl()}/deployments`, {headers: this.getHeaders()})
      .map((response: Response) => response.json())
      .catch(this.handleError);
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
      .get(`${this.getBaseUrl()}/deployments/filter`, options)
      .map((response: Response) => this.extractDeploymentsAndTotalCount(response))
      .catch(this.handleError);
  }

  getFilteredDeploymentsForCsvExport(filterString: string, sortCol: string, sortDir: string): Observable<string> {
    const params = new URLSearchParams();
    params.append('filters', filterString);
    params.append('colToSort', sortCol);
    params.append('sortDirection', sortDir);
    const options = new RequestOptions({
      search: params,
      headers: this.csvHeaders()
    });
    return this.http
      .get(`${this.getBaseUrl()}/deployments/filter`, options)
      .map((response: Response) => response.text())
      .catch(this.handleError);
  }

  get(deploymentId: number): Observable<Deployment> {
    return this.http
      .get(`${this.getBaseUrl()}/deployments/${deploymentId}`, {headers: this.getHeaders()})
      .map((response: Response) => response.json())
      .catch(this.handleError);
  }

  getWithActions(deploymentId: number): Observable<Deployment> {
    return this.http
      .get(`${this.getBaseUrl()}/deployments/${deploymentId}/withActions`, {headers: this.getHeaders()})
      .map((response: Response) => response.json())
      .catch(this.handleError);
  }

  createDeployment(deploymentRequest: DeploymentRequest): Observable<Deployment> {
    return this.http
      .post(`${this.getBaseUrl()}/deployments`, deploymentRequest, {headers: this.postHeaders()})
      .map((res: Response) => res.json())
      .catch(this.handleError);
  }

  cancelDeployment(deploymentId: number) {
    return this.http
      .put(`${this.getBaseUrl()}/deployments/${deploymentId}/updateState`, 'canceled', {headers: this.getHeaders()})
      .map(this.extractPayload)
      .catch(this.handleError);
  }

  confirmDeployment(deployment: Deployment) {
    return this.http
      .put(`${this.getBaseUrl()}/deployments/${deployment.id}/confirm`, deployment, {headers: this.getHeaders()})
      .map(this.extractPayload)
      .catch(this.handleError);
  }

  rejectDeployment(deploymentId: number) {
    return this.http
      .put(`${this.getBaseUrl()}/deployments/${deploymentId}/updateState`, 'rejected', {headers: this.getHeaders()})
      .map(this.extractPayload)
      .catch(this.handleError);
  }

  getAllDeploymentParameterKeys(): Observable<DeploymentParameter[]> {
    return this.http
      .get(`${this.getBaseUrl()}/deployments/deploymentParameterKeys/`, {headers: this.getHeaders()})
      .map((response: Response) => response.json())
      .catch(this.handleError);
  }

  canDeploy(resourceGroupId: number, contextIds: number[]): Observable<boolean> {
    const params = new URLSearchParams();
    contextIds.forEach((key) => params.append('contextId', String(key)));
    const options = new RequestOptions({
      search: params,
      headers: this.getHeaders()
    });
    return this.http
      .get(`${this.getBaseUrl()}/deployments/canDeploy/${resourceGroupId}`, options)
      .map((response: Response) => response.json())
      .catch(this.handleError);
  }

  canRequestDeployment(resourceGroupId: number, contextIds: number[]): Observable<boolean> {
    const params = new URLSearchParams();
    contextIds.forEach((key) => params.append('contextId', String(key)));
    const options = new RequestOptions({
      search: params,
      headers: this.getHeaders()
    });
    return this.http
      .get(`${this.getBaseUrl()}/deployments/canRequestDeployment/${resourceGroupId}`, options)
      .map((response: Response) => response.json())
      .catch(this.handleError);
  }

  canRequestDeployments(): Observable<boolean> {
    return this.http
      .get(`${this.getBaseUrl()}/deployments/canRequestDeployment/`, {headers: this.getHeaders()})
      .map((response: Response) => response.json())
      .catch(this.handleError);
  }

  getAllDeploymentFilterTypes(): Observable<DeploymentFilterType[]> {
    return this.http
      .get(`${this.getBaseUrl()}/deployments/deploymentFilterTypes/`, {headers: this.getHeaders()})
      .map((response: Response) => response.json())
      .catch(this.handleError);
  }

  getAllComparatorFilterOptions(): Observable<ComparatorFilterOption[]> {
    return this.http
      .get(`${this.getBaseUrl()}/deployments/comparatorFilterOptions/`, {headers: this.getHeaders()})
      .map((response: Response) => response.json())
      .catch(this.handleError);
  }

  getFilterOptionValues(filterName: string): Observable<string[]> {
    const param = new URLSearchParams();
    param.append('filterName', filterName);
    const options = new RequestOptions({
      search: param,
      headers: this.getHeaders()
    });
    return this.http
      .get(`${this.getBaseUrl()}/deployments/filterOptionValues/`, options)
      .map((response: Response) => response.json())
      .catch(this.handleError);
  }

  setDeploymentDate(deploymentId: number, deploymentDate: number) {
    return this.http
      .put(`${this.getBaseUrl()}/deployments/${deploymentId}/date`, deploymentDate, {headers: this.postHeaders()})
      .map(this.extractPayload)
      .catch(this.handleError);
  }

  isAngularDeploymentsGuiActive(): Observable<boolean> {
    return this.http
      .get(`${this.getBaseUrl()}/deployments/isAngularDeploymentsGuiActive/`, {headers: this.getHeaders()})
      .map((response: Response) => response.json())
      .catch(this.handleError);
  }

  private csvHeaders() {
    const headers = new Headers();
    headers.append('Accept', 'text/csv');
    return headers;
  }

  private extractDeploymentsAndTotalCount(res: Response) {
    const headerField: string = 'X-Total-Count';
    const ob: { deployments: Deployment[], total: number } = { deployments: [], total: 0 };
    ob.deployments = this.extractPayload(res);
    ob.total = res.headers.get(headerField) ? parseInt(res.headers.get(headerField), 10) : 0;
    return ob;
  }

}
