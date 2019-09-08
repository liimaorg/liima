import { Injectable } from '@angular/core';
import { HttpClient, HttpParams, HttpResponse, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map, catchError } from 'rxjs/operators';
import { ComparatorFilterOption } from './comparator-filter-option';
import { Deployment } from './deployment';
import { DeploymentFilterType } from './deployment-filter-type';
import { DeploymentRequest } from './deployment-request';
import { DeploymentParameter } from './deployment-parameter';
import { BaseService } from '../base/base.service';

@Injectable()
export class DeploymentService extends BaseService {

  constructor(private http: HttpClient) {
    super();
  }

  getAll(): Observable<Deployment[]> {
    return this.http
      .get<Deployment[]>(`${this.getBaseUrl()}/deployments`, {headers: this.getHeaders()})
      .pipe(catchError(this.handleError));
  }

  getFilteredDeployments(filterString: string, sortCol: string, sortDir: string, offset: number, maxResults: number): Observable<{ deployments: Deployment[], total: number }> {
    const params = new HttpParams();
    params.append('filters', filterString);
    params.append('colToSort', sortCol);
    params.append('sortDirection', sortDir);
    params.append('offset', String(offset));
    params.append('maxResults', String(maxResults));

    return this.http
      .get<Deployment[]>(`${this.getBaseUrl()}/deployments/filter`,
        {
          params: params,
          headers: this.getHeaders(),
          observe: 'response'
        })
      .pipe(
        map(data => this.extractDeploymentsAndTotalCount(data)),
        catchError(this.handleError)
      );
  }

  getFilteredDeploymentsForCsvExport(filterString: string, sortCol: string, sortDir: string): Observable<string> {
    const params = new HttpParams();
    params.append('filters', filterString);
    params.append('colToSort', sortCol);
    params.append('sortDirection', sortDir);

    return this.http
      .get<string>(`${this.getBaseUrl()}/deployments/filter`,
        {
          params: params,
          headers: this.csvHeaders()
        })
      .pipe(catchError(this.handleError));
  }

  get(deploymentId: number): Observable<Deployment> {
    return this.http
      .get<Deployment>(`${this.getBaseUrl()}/deployments/${deploymentId}`, {headers: this.getHeaders()})
      .pipe(catchError(this.handleError));
  }

  getWithActions(deploymentId: number): Observable<Deployment> {
    return this.http
      .get<Deployment>(`${this.getBaseUrl()}/deployments/${deploymentId}/withActions`, {headers: this.getHeaders()})
      .pipe(catchError(this.handleError));
  }

  createDeployment(deploymentRequest: DeploymentRequest): Observable<Deployment> {
    return this.http
      .post<Deployment>(`${this.getBaseUrl()}/deployments`, deploymentRequest, {headers: this.postHeaders()})
      .pipe(catchError(this.handleError));
  }

  cancelDeployment(deploymentId: number) {
    return this.http
      .put(`${this.getBaseUrl()}/deployments/${deploymentId}/updateState`, 'canceled', {headers: this.getHeaders()})
      .pipe(catchError(this.handleError));
  }

  confirmDeployment(deployment: Deployment) {
    return this.http
      .put(`${this.getBaseUrl()}/deployments/${deployment.id}/confirm`, deployment, {headers: this.getHeaders()})
      .pipe(catchError(this.handleError));
  }

  rejectDeployment(deploymentId: number) {
    return this.http
      .put(`${this.getBaseUrl()}/deployments/${deploymentId}/updateState`, 'rejected', {headers: this.getHeaders()})
      .pipe(catchError(this.handleError));
  }

  getAllDeploymentParameterKeys(): Observable<DeploymentParameter[]> {
    return this.http
      .get<DeploymentParameter[]>(`${this.getBaseUrl()}/deployments/deploymentParameterKeys/`, {headers: this.getHeaders()})
      .pipe(catchError(this.handleError));
  }

  canDeploy(resourceGroupId: number, contextIds: number[]): Observable<boolean> {
    const params = new HttpParams();
    contextIds.forEach((key) => params.append('contextId', String(key)));
    return this.http
      .get<boolean>(`${this.getBaseUrl()}/deployments/canDeploy/${resourceGroupId}`,
        {
          params: params,
          headers: this.getHeaders()
        })
      .pipe(catchError(this.handleError));
  }

  canRequestDeployment(resourceGroupId: number, contextIds: number[]): Observable<boolean> {
    const params = new HttpParams();
    contextIds.forEach((key) => params.append('contextId', String(key)));
    return this.http
      .get<boolean>(`${this.getBaseUrl()}/deployments/canRequestDeployment/${resourceGroupId}`,
        {
          params: params,
          headers: this.getHeaders()
        })
      .pipe(catchError(this.handleError));
  }

  canRequestDeployments(): Observable<boolean> {
    return this.http
      .get<boolean>(`${this.getBaseUrl()}/deployments/canRequestDeployment/`, {headers: this.getHeaders()})
      .pipe(catchError(this.handleError));
  }

  getAllDeploymentFilterTypes(): Observable<DeploymentFilterType[]> {
    return this.http
      .get<DeploymentFilterType[]>(`${this.getBaseUrl()}/deployments/deploymentFilterTypes/`, {headers: this.getHeaders()})
      .pipe(catchError(this.handleError));
  }

  getAllComparatorFilterOptions(): Observable<ComparatorFilterOption[]> {
    return this.http
      .get<ComparatorFilterOption[]>(`${this.getBaseUrl()}/deployments/comparatorFilterOptions/`, {headers: this.getHeaders()})
      .pipe(catchError(this.handleError));
  }

  getFilterOptionValues(filterName: string): Observable<string[]> {
    const param = new HttpParams();
    param.append('filterName', filterName);
    return this.http
      .get<string[]>(`${this.getBaseUrl()}/deployments/filterOptionValues/`,
        {
          params: param,
          headers: this.getHeaders()
        })
      .pipe(catchError(this.handleError));
  }

  setDeploymentDate(deploymentId: number, deploymentDate: number) {
    return this.http
      .put(`${this.getBaseUrl()}/deployments/${deploymentId}/date`, deploymentDate, {headers: this.postHeaders()})
      .pipe(catchError(this.handleError));
  }

  isAngularDeploymentsGuiActive(): Observable<boolean> {
    return this.http
      .get<boolean>(`${this.getBaseUrl()}/deployments/isAngularDeploymentsGuiActive/`, {headers: this.getHeaders()})
      .pipe(catchError(this.handleError));
  }

  private csvHeaders() {
    const headers = new HttpHeaders();
    headers.append('Accept', 'text/csv');
    return headers;
  }

  private extractDeploymentsAndTotalCount(res: HttpResponse<Deployment[]>) {
    const headerField: string = 'X-Total-Count';
    const ob: { deployments: Deployment[], total: number } = { deployments: [], total: 0 };
    ob.deployments = res.body;
    ob.total = res.headers.get(headerField) ? parseInt(res.headers.get(headerField), 10) : 0;
    return ob;
  }

}
