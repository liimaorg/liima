import { HttpErrorResponse, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { throwError } from 'rxjs';
import * as _ from 'lodash-es';

@Injectable()
export class BaseService {
  private baseUrl = '/AMW_rest/resources';

  public getBaseUrl(): string {
    return this.baseUrl;
  }

  public getHeaders(): HttpHeaders {
    return new HttpHeaders().append('Accept', 'application/json');
  }

  public postHeaders() {
    let headers = new HttpHeaders();
    headers = headers.append('Content-Type', 'application/json');
    headers = headers.append('Accept', 'application/json');
    return headers;
  }

  public handleError(response: HttpErrorResponse) {
    let errorMsg = 'Error retrieving your data';
    if (response.error) {
      try {
        errorMsg = _.escape(response.error.message);
      } catch (e) {
        console.log(e);
      }
    }
    console.error(response);
    // throw an application level error
    return throwError(() => errorMsg);
  }
}
