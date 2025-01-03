import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { ResourceTemplate } from './resource-template';
import { catchError } from 'rxjs/operators';
import { Observable } from 'rxjs';
import { BaseService } from '../base/base.service';

@Injectable({ providedIn: 'root' })
export class ResourceTemplatesService extends BaseService {
  constructor(private http: HttpClient) {
    super();
  }

  getResourceTemplates(id: number): Observable<ResourceTemplate[]> {
    return this.http
      .get<ResourceTemplate[]>(`${this.getBaseUrl()}/resources/templates/${id}`, {
        headers: this.getHeaders(),
      })
      .pipe(catchError(this.handleError));
  }

  getResourceTypeTemplates(id: number): Observable<ResourceTemplate[]> {
    return this.http
      .get<ResourceTemplate[]>(`${this.getBaseUrl()}/resources/templates/resourceType/${id}`, {
        headers: this.getHeaders(),
      })
      .pipe(catchError(this.handleError));
  }
}
