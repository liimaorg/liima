import { Injectable } from '@angular/core';
import { BaseService } from '../../base/base.service';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { Release } from './release';

@Injectable({ providedIn: 'root' })
export class ReleasesService extends BaseService {
  constructor(private http: HttpClient) {
    super();
  }

  canCreateRelease(): Observable<boolean> {
    return this.http
      .get<boolean>(`${this.getBaseUrl()}/releases/canCreateRelease`, {
        headers: this.getHeaders(),
      })
      .pipe(catchError(this.handleError));
  }

  create(release: Release) {
    // TODO
    return this.http
      .post<boolean>(`${this.getBaseUrl()}/releases/canCreateRelease`, {
        headers: this.getHeaders(),
      })
      .pipe(catchError(this.handleError));
  }
}
