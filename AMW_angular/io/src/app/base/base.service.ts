import { HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import * as _ from 'lodash';

@Injectable()
export class BaseService {

  private baseUrl: string = '/AMW_rest/resources';

  public getBaseUrl(): string {
    return this.baseUrl;
  }

  public getHeaders() {
    const headers = new HttpHeaders();
    headers.append('Accept', 'application/json');
    return headers;
  }

  public postHeaders() {
    const headers = new HttpHeaders();
    headers.append('Content-Type', 'application/json');
    headers.append('Accept', 'application/json');
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
    console.error(errorMsg);
    // throw an application level error
    return Observable.throw(errorMsg);
  }
}
