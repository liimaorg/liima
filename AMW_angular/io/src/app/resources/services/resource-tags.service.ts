import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { BaseService } from '../../base/base.service';
import { ResourceTag } from '../models/resource-tag';

export interface TagRequest {
  label: string;
  tagDate: Date;
}

@Injectable({ providedIn: 'root' })
export class ResourceTagsService extends BaseService {
  private http = inject(HttpClient);

  getResourceTags(resourceId: number): Observable<ResourceTag[]> {
    return this.http
      .get<ResourceTag[]>(`${this.getBaseUrl()}/resources/${resourceId}/tags`, {
        headers: this.getHeaders(),
      })
      .pipe(catchError(this.handleError));
  }

  createTag(resourceId: number, tagRequest: TagRequest): Observable<ResourceTag> {
    return this.http
      .post<ResourceTag>(`${this.getBaseUrl()}/resources/${resourceId}/tags`, tagRequest, {
        headers: this.getHeaders(),
      })
      .pipe(catchError(this.handleError));
  }
}
