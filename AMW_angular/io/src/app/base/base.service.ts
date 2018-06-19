import {Headers, Http, Response} from "@angular/http";
import {Injectable} from "@angular/core";
import {Observable} from "rxjs/Rx";
import * as _ from "lodash";

@Injectable()
export class BaseService {
  private baseUrl: string = '/AMW_rest/resources';

  public getbaseUrl(): string {
    return this.baseUrl;
  }

  public extractPayload(res: Response) {
    return res.text() ? res.json() : {};
  }

  public getHeaders() {
    const headers = new Headers();
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
