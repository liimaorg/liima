import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { BaseService } from '../../base/base.service';
import { ApplicationRelation } from '../models/application-relation';

@Injectable({ providedIn: 'root' })
export class ResourceApplicationsService extends BaseService {
  private http = inject(HttpClient);

  getApplicationsForResource(resourceId: number): Observable<ApplicationRelation[]> {
    return this.http
      .get<ApplicationRelation[]>(`${this.getBaseUrl()}/resources/${resourceId}/applications`, {
        headers: this.getHeaders(),
      })
      .pipe(catchError(this.handleError));
  }

  removeApplication(resourceId: number, relationId: number): Observable<void> {
    return this.http
      .delete<void>(`${this.getBaseUrl()}/resources/${resourceId}/applications/${relationId}`, {
        headers: this.getHeaders(),
      })
      .pipe(catchError(this.handleError));
  }
}
