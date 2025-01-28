import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, Subject } from 'rxjs';
import { toSignal } from '@angular/core/rxjs-interop';
import { catchError, shareReplay, switchMap } from 'rxjs/operators';
import { BaseService } from '../base/base.service';
import { RelatedResource } from './related-resource';

@Injectable({ providedIn: 'root' })
export class RelatedResourcesService extends BaseService {
  private reload$ = new Subject<RelatedResource[]>();
  private resourceId$: Subject<number> = new Subject<number>();
  private relatedResources$: Observable<RelatedResource[]> = this.resourceId$.pipe(
    switchMap((id: number) => this.getRelationsByResourceId(id)),
    shareReplay(1),
  );

  relatedResources = toSignal(this.relatedResources$, { initialValue: [] as RelatedResource[] });

  constructor(private http: HttpClient) {
    super();
  }

  setResourceId(id: number) {
    this.resourceId$.next(id);
  }

  getRelationsByResourceId(id: number): Observable<RelatedResource[]> {
    return this.http
      .get<RelatedResource[]>(`${this.getBaseUrl()}/resources/relations/resource/${id}`, {
        headers: this.getHeaders(),
      })
      .pipe(catchError(this.handleError));
  }

  refreshData() {
    this.reload$.next([]);
  }
}
