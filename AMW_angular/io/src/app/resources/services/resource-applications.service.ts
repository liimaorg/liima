import { inject, Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, Subject } from 'rxjs';
import { catchError, finalize, shareReplay, switchMap } from 'rxjs/operators';
import { BaseService } from '../../base/base.service';
import { ApplicationRelation } from '../models/application-relation';
import { toSignal } from '@angular/core/rxjs-interop';

@Injectable({ providedIn: 'root' })
export class ResourceApplicationsService extends BaseService {
  private http = inject(HttpClient);

  private loading = signal(false);
  isLoading = this.loading.asReadonly();

  private resourceId$ = new Subject<number>();

  private applicationsForResource$ = this.resourceId$.pipe(
    switchMap((id: number) => {
      this.loading.set(true);
      return this.getApplicationsForResource(id).pipe(finalize(() => this.loading.set(false)));
    }),
    shareReplay(1),
  );

  applications = toSignal(this.applicationsForResource$, { initialValue: [] as ApplicationRelation[] });

  setResourceId(id: number) {
    this.resourceId$.next(id);
  }

  getApplicationsForResource(resourceId: number): Observable<ApplicationRelation[]> {
    return this.http
      .get<ApplicationRelation[]>(`${this.getBaseUrl()}/resources/${resourceId}/applications`, {
        headers: this.getHeaders(),
      })
      .pipe(catchError(this.handleError));
  }

  addApplication(resourceId: number, applicationGroupId: number): Observable<void> {
    return this.http
      .post<void>(
        `${this.getBaseUrl()}/resources/${resourceId}/applications?applicationGroupId=${applicationGroupId}`,
        null,
        {
          headers: this.postHeaders(),
        },
      )
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
