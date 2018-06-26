import { Headers, Response } from '@angular/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/Rx';
import * as _ from 'lodash';

@Injectable()
export class BaseService {

  private baseUrl: string = '/AMW_rest/resources';

  public getBaseUrl(): string {
    return this.baseUrl;
  }

  // to json without throwing an error if response is empty
  public extractPayload(res: Response) {
    return res.text() ? res.json() : {};
  }

  public getHeaders() {
    const headers = new Headers();
    headers.append('Accept', 'application/json');
    return headers;
  }

  public postHeaders() {
    const headers = new Headers();
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
