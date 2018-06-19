import { Injectable } from '@angular/core';
import { Http, Response } from '@angular/http';
import { Observable } from 'rxjs/Rx';
import { Auditviewentrytype } from './auditview-entry-type';
import { BaseService } from "../base/base.service";

@Injectable()
export class AuditviewService extends BaseService {

  constructor(private http: Http) {
    super();
  }

  getAuditLogForResource(resourceId: number): Observable<Auditviewentrytype[]> {
    const resource$ = this.http
      .get(`${this.getBaseUrl()}/auditview/resource/${resourceId}`,{headers: this.getHeaders()})
      .map((response: Response) => this.extractPayload(response))
      .catch(this.handleError);
    return resource$;
  }

}
