import { inject, Injectable, signal } from '@angular/core';
import { BaseService } from '../../base/base.service';
import { HttpClient } from '@angular/common/http';
import { toSignal } from '@angular/core/rxjs-interop';
import { Observable, startWith, Subject } from 'rxjs';
import { GroupedResourceRelations } from '../models/resource-relation';
import { catchError, finalize, shareReplay, switchMap } from 'rxjs/operators';

const EMPTY_GROUPED_RELATIONS: GroupedResourceRelations = {
  runtime: [],
  consumed: [],
  provided: [],
  unresolved: [],
};

@Injectable({ providedIn: 'root' })
export class ResourceRelationsService extends BaseService {
  private http = inject(HttpClient);

  private loadingRelations = signal(false);
  isLoadingRelations = this.loadingRelations.asReadonly();

  private relations$: Subject<number> = new Subject<number>();

  private relationsForResource$: Observable<GroupedResourceRelations> = this.relations$.pipe(
    switchMap((id: number) => {
      this.loadingRelations.set(true);
      return this.getResourceRelations(id).pipe(finalize(() => this.loadingRelations.set(false)));
    }),
    startWith(EMPTY_GROUPED_RELATIONS),
    shareReplay(1),
  );

  relations = toSignal(this.relationsForResource$, { initialValue: EMPTY_GROUPED_RELATIONS });

  setIdForResourceRelations(id: number) {
    this.relations$.next(id);
  }

  getResourceRelations(id: number): Observable<GroupedResourceRelations> {
    return this.http
      .get<GroupedResourceRelations>(`${this.getBaseUrl()}/resources/${id}/relations`, {
        headers: this.getHeaders(),
      })
      .pipe(catchError(this.handleError));
  }
}
