import { inject, Injectable, signal } from '@angular/core';
import { BaseService } from '../../base/base.service';
import { HttpClient } from '@angular/common/http';
import { toSignal } from '@angular/core/rxjs-interop';
import { Observable, startWith, Subject } from 'rxjs';
import { GroupedResourceRelations } from '../models/resource-relation';
import { Property } from '../models/property';
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

  private loadingTypeRelations = signal(false);
  isLoadingTypeRelations = this.loadingTypeRelations.asReadonly();

  private typeRelations$: Subject<number> = new Subject<number>();

  private relationsForResourceType$: Observable<GroupedResourceRelations> = this.typeRelations$.pipe(
    switchMap((id: number) => {
      this.loadingTypeRelations.set(true);
      return this.getResourceTypeRelations(id).pipe(finalize(() => this.loadingTypeRelations.set(false)));
    }),
    startWith(EMPTY_GROUPED_RELATIONS),
    shareReplay(1),
  );

  typeRelations = toSignal(this.relationsForResourceType$, { initialValue: EMPTY_GROUPED_RELATIONS });

  setIdForResourceTypeRelations(id: number) {
    this.typeRelations$.next(id);
  }

  getResourceTypeRelations(id: number): Observable<GroupedResourceRelations> {
    return this.http
      .get<GroupedResourceRelations>(`${this.getBaseUrl()}/resourceTypes/${id}/relations`, {
        headers: this.getHeaders(),
      })
      .pipe(catchError(this.handleError));
  }

  getResourceTypeRelationProperties(
    resourceTypeId: number,
    relTypeId: number,
    contextId: number,
  ): Observable<Property[]> {
    return this.http
      .get<
        Property[]
      >(`${this.getBaseUrl()}/resourceTypes/${resourceTypeId}/relations/${relTypeId}/properties?contextId=${contextId}`, { headers: this.getHeaders() })
      .pipe(catchError(this.handleError));
  }

  private loadingRelationProperties = signal(false);
  isLoadingRelationProperties = this.loadingRelationProperties.asReadonly();

  private relationProperties$: Subject<{ resourceId: number; relationId: number; contextId: number }> = new Subject<{
    resourceId: number;
    relationId: number;
    contextId: number;
  }>();

  private relationPropertiesForResource$: Observable<Property[]> = this.relationProperties$.pipe(
    switchMap(({ resourceId, relationId, contextId }) => {
      this.loadingRelationProperties.set(true);
      return this.getResourceRelationProperties(resourceId, relationId, contextId).pipe(
        finalize(() => this.loadingRelationProperties.set(false)),
      );
    }),
    startWith([]),
    shareReplay(1),
  );

  relationProperties = toSignal(this.relationPropertiesForResource$, { initialValue: [] as Property[] });

  setIdsForRelationProperties(resourceId: number, relationId: number, contextId: number) {
    this.relationProperties$.next({ resourceId, relationId, contextId });
  }

  private loadingTypeRelationProperties = signal(false);
  isLoadingTypeRelationProperties = this.loadingTypeRelationProperties.asReadonly();

  private typeRelationProperties$: Subject<{ resourceTypeId: number; relTypeId: number; contextId: number }> =
    new Subject<{
      resourceTypeId: number;
      relTypeId: number;
      contextId: number;
    }>();

  private typeRelationPropertiesForType$: Observable<Property[]> = this.typeRelationProperties$.pipe(
    switchMap(({ resourceTypeId, relTypeId, contextId }) => {
      this.loadingTypeRelationProperties.set(true);
      return this.getResourceTypeRelationProperties(resourceTypeId, relTypeId, contextId).pipe(
        finalize(() => this.loadingTypeRelationProperties.set(false)),
      );
    }),
    startWith([]),
    shareReplay(1),
  );

  typeRelationProperties = toSignal(this.typeRelationPropertiesForType$, { initialValue: [] as Property[] });

  setIdsForTypeRelationProperties(resourceTypeId: number, relTypeId: number, contextId: number) {
    this.typeRelationProperties$.next({ resourceTypeId, relTypeId, contextId });
  }

  getResourceRelationProperties(resourceId: number, relationId: number, contextId: number): Observable<Property[]> {
    return this.http
      .get<Property[]>(
        `${this.getBaseUrl()}/resources/${resourceId}/relations/${relationId}/properties?contextId=${contextId}`,
        {
          headers: this.getHeaders(),
        },
      )
      .pipe(catchError(this.handleError));
  }
}
