import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { BaseService } from '../base/base.service';
import { AuditLogEntry } from './auditview-entry';

@Injectable({
  providedIn: 'root',
})
export class AuditviewService extends BaseService {
  private http = inject(HttpClient);

  getAuditLogForResource(resourceId: number): Observable<AuditLogEntry[]> {
    return this.http
      .get<AuditLogEntry[]>(`${this.getBaseUrl()}/auditview/resource/${resourceId}`, {
        headers: this.getHeaders(),
      })
      .pipe(catchError(this.handleError));
  }
}
