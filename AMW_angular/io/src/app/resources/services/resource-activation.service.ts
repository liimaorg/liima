import { inject, Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, Subject } from 'rxjs';
import { catchError, finalize, shareReplay, switchMap } from 'rxjs/operators';
import { BaseService } from '../../base/base.service';
import { toSignal } from '@angular/core/rxjs-interop';

export interface ResourceActivation {
  resourceGroupId: number;
  resourceGroupName: string;
  active: boolean;
}

export interface ResourceActivationsRequest {
  activeResourceGroupIds: number[];
}

@Injectable({ providedIn: 'root' })
export class ResourceActivationService extends BaseService {
  private http = inject(HttpClient);

  private loading = signal(false);
  isLoading = this.loading.asReadonly();

  private activationParams$ = new Subject<{ resourceId: number; relationId: number; contextId: number }>();

  private activationsForRelation$ = this.activationParams$.pipe(
    switchMap((params) => {
      this.loading.set(true);
      return this.getActivations(params.resourceId, params.relationId, params.contextId).pipe(
        finalize(() => this.loading.set(false))
      );
    }),
    shareReplay(1)
  );

  activations = toSignal(this.activationsForRelation$, { initialValue: [] as ResourceActivation[] });

  setRelationParams(resourceId: number, relationId: number, contextId: number) {
    this.activationParams$.next({ resourceId, relationId, contextId });
  }

  getActivations(resourceId: number, relationId: number, contextId: number): Observable<ResourceActivation[]> {
    return this.http
      .get<ResourceActivation[]>(
        `${this.getBaseUrl()}/resources/${resourceId}/relations/${relationId}/activations?contextId=${contextId}`,
        { headers: this.getHeaders() }
      )
      .pipe(catchError(this.handleError));
  }

  updateActivations(
    resourceId: number,
    relationId: number,
    contextId: number,
    request: ResourceActivationsRequest
  ): Observable<void> {
    return this.http
      .put<void>(
        `${this.getBaseUrl()}/resources/${resourceId}/relations/${relationId}/activations?contextId=${contextId}`,
        request,
        { headers: this.getHeaders() }
      )
      .pipe(catchError(this.handleError));
  }
}
