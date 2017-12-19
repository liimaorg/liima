import { Injectable } from '@angular/core';
import { Http, Response, Headers, URLSearchParams, RequestOptions } from '@angular/http';
import { Observable } from 'rxjs/Rx';
import { Environment } from './environment';
import * as _ from 'lodash';

@Injectable()
export class EnvironmentService {
  private baseUrl: string = '/AMW_rest/resources';

  constructor(private http: Http) {
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
      .get(`${this.baseUrl}/environments`, options)
      .map((response: Response) => response.json())
      .catch(handleError);
  }

  private getHeaders() {
    const headers = new Headers();
    headers.append('Accept', 'application/json');
    return headers;
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
