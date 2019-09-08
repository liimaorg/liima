import { Injectable } from '@angular/core';
import { HttpClient, HttpParams, HttpResponse, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { Auditviewentrytype } from './auditview-entry-type';
import { BaseService } from "../base/base.service";

@Injectable()
export class AuditviewService extends BaseService {

  constructor(private http: HttpClient) {
    super();
  }

  getAuditLogForResource(resourceId: number): Observable<Auditviewentrytype[]> {
    const resource$ = this.http
      .get<Auditviewentrytype[]>(`${this.getBaseUrl()}/auditview/resource/${resourceId}`,{headers: this.getHeaders()})
      .pipe(catchError(this.handleError));
    return resource$;
  }

}
