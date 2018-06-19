import { Injectable } from '@angular/core';
import { Http, Response, Headers, RequestOptions, URLSearchParams } from '@angular/http';
import { Observable } from 'rxjs/Rx';
import { ComparatorFilterOption } from './comparator-filter-option';
import { Deployment } from './deployment';
import { DeploymentFilterType } from './deployment-filter-type';
import { DeploymentRequest } from './deployment-request';
import { DeploymentParameter } from './deployment-parameter';
import { DeploymentDetail } from './deployment-detail';
import { BaseService } from '../base/base.service';


@Injectable()
export class DeploymentService extends BaseService {

  constructor(private http: Http) {
    super();
  }

  getAll(): Observable<Deployment[]> {
    return this.http
      .get(`${this.getbaseUrl()}/deployments`, {headers: this.getHeaders()})
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
      .get(`${this.getbaseUrl()}/deployments/filter`, options)
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
      .get(`${this.getbaseUrl()}/deployments/filter`, options)
      .map((response: Response) => response.text())
      .catch(this.handleError);
  }

  get(deploymentId: number): Observable<Deployment> {
    return this.http
      .get(`${this.getbaseUrl()}/deployments/${deploymentId}`, {headers: this.getHeaders()})
      .map((response: Response) => response.json())
      .catch(this.handleError);
  }

  getWithActions(deploymentId: number): Observable<Deployment> {
    return this.http
      .get(`${this.getbaseUrl()}/deployments/${deploymentId}/withActions`, {headers: this.getHeaders()})
      .map((response: Response) => response.json())
      .catch(this.handleError);
  }

  createDeployment(deploymentRequest: DeploymentRequest): Observable<Deployment> {
    return this.http
      .post(`${this.getbaseUrl()}/deployments`, deploymentRequest, {headers: this.postHeaders()})
      .map((res: Response) => res.json())
      .catch(this.handleError);
  }

  cancelDeployment(deploymentId: number) {
    return this.http
      .put(`${this.getbaseUrl()}/deployments/${deploymentId}/updateState`, 'canceled', {headers: this.getHeaders()})
      .map(this.extractPayload)
      .catch(this.handleError);
  }

  confirmDeployment(deploymentDetail: DeploymentDetail) {
    return this.http
      .put(`${this.getbaseUrl()}/deployments/${deploymentDetail.deploymentId}/confirm`, deploymentDetail, {headers: this.getHeaders()})
      .map(this.extractPayload)
      .catch(this.handleError);
  }

  rejectDeployment(deploymentId: number) {
    return this.http
      .put(`${this.getbaseUrl()}/deployments/${deploymentId}/updateState`, 'rejected', {headers: this.getHeaders()})
      .map(this.extractPayload)
      .catch(this.handleError);
  }

  getAllDeploymentParameterKeys(): Observable<DeploymentParameter[]> {
    return this.http
      .get(`${this.getbaseUrl()}/deployments/deploymentParameterKeys/`, {headers: this.getHeaders()})
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
      .get(`${this.getbaseUrl()}/deployments/canDeploy/${resourceGroupId}`, options)
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
      .get(`${this.getbaseUrl()}/deployments/canRequestDeployment/${resourceGroupId}`, options)
      .map((response: Response) => response.json())
      .catch(this.handleError);
  }

  canRequestDeployments(): Observable<boolean> {
    return this.http
      .get(`${this.getbaseUrl()}/deployments/canRequestDeployment/`, {headers: this.getHeaders()})
      .map((response: Response) => response.json())
      .catch(this.handleError);
  }

  getAllDeploymentFilterTypes(): Observable<DeploymentFilterType[]> {
    return this.http
      .get(`${this.getbaseUrl()}/deployments/deploymentFilterTypes/`, {headers: this.getHeaders()})
      .map((response: Response) => response.json())
      .catch(this.handleError);
  }

  getAllComparatorFilterOptions(): Observable<ComparatorFilterOption[]> {
    return this.http
      .get(`${this.getbaseUrl()}/deployments/comparatorFilterOptions/`, {headers: this.getHeaders()})
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
      .get(`${this.getbaseUrl()}/deployments/filterOptionValues/`, options)
      .map((response: Response) => response.json())
      .catch(this.handleError);
  }

  getDeploymentDetail(deploymentId: number): Observable<DeploymentDetail> {
    return this.http
      .get(`${this.getbaseUrl()}/deployments/${deploymentId}/detail`, {headers: this.getHeaders()})
      .map((response: Response) => response.json())
      .catch(this.handleError);
  }

  setDeploymentDate(deploymentId: number, deploymentDate: number) {
    return this.http
      .put(`${this.getbaseUrl()}/deployments/${deploymentId}/date`, deploymentDate, {headers: this.postHeaders()})
      .map(this.extractPayload)
      .catch(this.handleError);
  }

  isAngularDeploymentsGuiActive(): Observable<boolean> {
    return this.http
      .get(`${this.getbaseUrl()}/deployments/isAngularDeploymentsGuiActive/`, {headers: this.getHeaders()})
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
