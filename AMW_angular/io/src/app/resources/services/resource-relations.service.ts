import { inject, Injectable, signal } from '@angular/core';
import { BaseService } from '../../base/base.service';
import { HttpClient } from '@angular/common/http';
import { toSignal } from '@angular/core/rxjs-interop';
import { Observable, startWith, Subject } from 'rxjs';
import { ResourceRelation } from '../models/resource-relation';
import { catchError, finalize, shareReplay, switchMap } from 'rxjs/operators';

@Injectable({ providedIn: 'root' })
export class ResourceRelationsService extends BaseService {
  private http = inject(HttpClient);

  private loadingRelations = signal(false);
  isLoadingRelations = this.loadingRelations.asReadonly();

  private relations$: Subject<number> = new Subject<number>();

  private relationsForResource$: Observable<ResourceRelation[]> = this.relations$.pipe(
    switchMap((id: number) => {
      this.loadingRelations.set(true);
      return this.getResourceRelations(id).pipe(finalize(() => this.loadingRelations.set(false)));
    }),
    startWith([]),
    shareReplay(1),
  );

  relations = toSignal(this.relationsForResource$, { initialValue: [] as ResourceRelation[] });

  setIdForResourceRelations(id: number) {
    this.relations$.next(id);
  }

  getResourceRelations(id: number): Observable<ResourceRelation[]> {
    return this.http
      .get<ResourceRelation[]>(`${this.getBaseUrl()}/resources/${id}/relations`, {
        headers: this.getHeaders(),
      })
      .pipe(catchError(this.handleError));
  }
}
