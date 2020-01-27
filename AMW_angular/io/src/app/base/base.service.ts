import { HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { throwError } from 'rxjs';
import * as _ from 'lodash';

@Injectable()
export class BaseService {
  private baseUrl: string = '/AMW_rest/resources';

  public getBaseUrl(): string {
    return this.baseUrl;
  }

  public getHeaders(): HttpHeaders {
    let headers = new HttpHeaders();
    headers = headers.append('Accept', 'application/json');
    return headers;
  }

  public postHeaders() {
    let headers = new HttpHeaders();
    headers = headers.append('Content-Type', 'application/json');
    headers = headers.append('Accept', 'application/json');
    return headers;
  }

  public handleError(error: any) {
    let errorMsg = 'Error retrieving your data';
    if (error._body) {
      try {
        errorMsg = _.escape(JSON.parse(error._body).message);
      } catch (e) {
        console.log(e);
      }
    }
    console.error(error);
    // throw an application level error
    return throwError(errorMsg);
  }
}
