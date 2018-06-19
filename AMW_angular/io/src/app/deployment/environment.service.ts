import { Injectable } from '@angular/core';
import { Http, Response, URLSearchParams, RequestOptions } from '@angular/http';
import { Observable } from 'rxjs/Rx';
import { Environment } from './environment';
import { BaseService } from '../base/base.service';

@Injectable()
export class EnvironmentService extends BaseService {

  constructor(private http: Http) {
    super();
  }

  getAll(): Observable<Environment[]> {
    return this.getEnvironments(false);
  }

  getAllIncludingGroups(): Observable<Environment[]> {
    return this.getEnvironments(true);
  }

  private getEnvironments(includingGroups: boolean): Observable<Environment[]> {
    const params: URLSearchParams = new URLSearchParams();
    if (includingGroups) {
      params.set('includingGroups', 'true');
    }
    const options = new RequestOptions({
      search: params,
      headers: this.getHeaders()
    });
    return this.http
      .get(`${this.getBaseUrl()}/environments`, options)
      .map((response: Response) => response.json())
      .catch(this.handleError);
  }

}
