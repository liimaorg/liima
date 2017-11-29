import { Injectable } from '@angular/core';
import { Http, Response, Headers, RequestOptions, URLSearchParams } from '@angular/http';
import { Observable } from 'rxjs/Rx';
import { Auditviewentrytype } from './auditview-entry-type';

@Injectable()
export class AuditviewService {
  private baseUrl: string = '/AMW_rest/resources';

  constructor(private http: Http) {

  }

  getAuditLogForResource(resourceId: number, contextId: number): Observable<Auditviewentrytype[]> {
    let params = new URLSearchParams();
    params.append('contextId', String(contextId));
    let options = new RequestOptions({
      search: params,
      headers: this.getHeaders()
    });

    let resource$ = this.http
      .get(`${this.baseUrl}/auditview/resource/${resourceId}`, options)
      .map((response: Response) => this.extractPayload(response))
      .catch(handleError);
    return resource$;
  }

  private extractPayload(res: Response) {
    return res.text() ? res.json() : {};
  }

  private getHeaders() {
    let headers = new Headers();
    headers.append('Accept', 'application/json');
    return headers;
  }

}

function handleError(error: any) {
  let errorMsg = 'Error retrieving your data';
  if (error._body) {
    try {
      errorMsg = JSON.parse(error._body).message;
    } catch (e) {
      console.log(e);
    }
  }
  console.error(errorMsg);
  // throw an application level error
  return Observable.throw(errorMsg);
}
