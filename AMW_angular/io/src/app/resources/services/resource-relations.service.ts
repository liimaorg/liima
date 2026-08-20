import { inject, Injectable, signal } from '@angular/core';
import { BaseService } from '../../base/base.service';
import { HttpClient, HttpParams } from '@angular/common/http';
import { toSignal } from '@angular/core/rxjs-interop';
import { Observable, of, startWith, Subject } from 'rxjs';
import { GroupedResourceRelations } from '../models/resource-relation';
import { Property } from '../models/property';
import { PropertyUpdate } from './resource-properties.service';
import { catchError, finalize, shareReplay, switchMap } from 'rxjs/operators';
import { ResourceTemplate } from '../models/resource-template';

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
      return this.getResourceRelations(id).pipe(
        startWith(EMPTY_GROUPED_RELATIONS),
        finalize(() => this.loadingRelations.set(false)),
        catchError(() => of(EMPTY_GROUPED_RELATIONS)),
      );
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
      return this.getResourceTypeRelations(id).pipe(
        startWith(EMPTY_GROUPED_RELATIONS),
        finalize(() => this.loadingTypeRelations.set(false)),
        catchError(() => of(EMPTY_GROUPED_RELATIONS)),
      );
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
      .get<Property[]>(
        `${this.getBaseUrl()}/resourceTypes/${resourceTypeId}/relations/${relTypeId}/properties?contextId=${contextId}`,
        { headers: this.getHeaders() },
      )
      .pipe(catchError(this.handleError));
  }

  private loadingRelationProperties = signal(false);
  private loadingTypeRelationProperties = signal(false);
  private relationPropertiesRequestId = 0;
  private typeRelationPropertiesRequestId = 0;
  isLoadingRelationProperties = this.loadingRelationProperties.asReadonly();
  isLoadingTypeRelationProperties = this.loadingTypeRelationProperties.asReadonly();

  private loadingRelationTemplates = signal(false);
  private relationTemplatesRequestId = 0;
  isLoadingRelationTemplates = this.loadingRelationTemplates.asReadonly();

  private relationProperties$: Subject<{ resourceId: number; relationId: number; contextId: number }> = new Subject<{
    resourceId: number;
    relationId: number;
    contextId: number;
  }>();

  private resourceRelationTemplates$: Subject<{ resourceId: number; relationId: number }> = new Subject<{
    resourceId: number;
    relationId: number;
  }>();

  private resourceTypeRelationTemplates$: Subject<{ resourceTypeId: number; relationId: number }> = new Subject<{
    resourceTypeId: number;
    relationId: number;
  }>();

  private relationPropertiesForResource$: Observable<Property[]> = this.relationProperties$.pipe(
    switchMap(({ resourceId, relationId, contextId }) => {
      const requestId = ++this.relationPropertiesRequestId;
      this.loadingRelationProperties.set(true);
      return this.getResourceRelationProperties(resourceId, relationId, contextId).pipe(
        finalize(() => {
          if (requestId === this.relationPropertiesRequestId) {
            this.loadingRelationProperties.set(false);
          }
        }),
        catchError(() => of([] as Property[])),
      );
    }),
    startWith([]),
    shareReplay(1),
  );

  private relationTemplatesForResource$: Observable<ResourceTemplate[]> = this.resourceRelationTemplates$.pipe(
    switchMap(({ resourceId, relationId }) => {
      const requestId = ++this.relationTemplatesRequestId;
      this.loadingRelationTemplates.set(true);
      return this.getResourceRelationTemplates(resourceId, relationId).pipe(
        finalize(() => {
          if (requestId === this.relationTemplatesRequestId) {
            this.loadingRelationTemplates.set(false);
          }
        }),
        catchError(() => of([] as ResourceTemplate[])),
      );
    }),
    startWith([]),
    shareReplay(1),
  );

  private relationTemplatesForResourceType$: Observable<ResourceTemplate[]> = this.resourceTypeRelationTemplates$.pipe(
    switchMap(({ resourceTypeId, relationId }) => {
      return this.getResourceTypeRelationTemplates(resourceTypeId, relationId).pipe(
        catchError(() => of([] as ResourceTemplate[])),
      );
    }),
    startWith([]),
    shareReplay(1),
  );

  relationProperties = toSignal(this.relationPropertiesForResource$, { initialValue: [] as Property[] });
  resourceRelationTemplates = toSignal(this.relationTemplatesForResource$, { initialValue: [] as ResourceTemplate[] });
  resourceTypeRelationTemplates = toSignal(this.relationTemplatesForResourceType$, {
    initialValue: [] as ResourceTemplate[],
  });

  setIdsForRelationProperties(resourceId: number, relationId: number, contextId: number) {
    this.relationProperties$.next({ resourceId, relationId, contextId });
  }

  setIdsForResourceRelationTemplates(resourceId: number, relationId: number) {
    this.resourceRelationTemplates$.next({ resourceId, relationId });
  }

  private typeRelationProperties$: Subject<{ resourceTypeId: number; relTypeId: number; contextId: number }> =
    new Subject<{
      resourceTypeId: number;
      relTypeId: number;
      contextId: number;
    }>();

  private typeRelationPropertiesForType$: Observable<Property[]> = this.typeRelationProperties$.pipe(
    switchMap(({ resourceTypeId, relTypeId, contextId }) => {
      const requestId = ++this.typeRelationPropertiesRequestId;
      this.loadingTypeRelationProperties.set(true);
      return this.getResourceTypeRelationProperties(resourceTypeId, relTypeId, contextId).pipe(
        finalize(() => {
          if (requestId === this.typeRelationPropertiesRequestId) {
            this.loadingTypeRelationProperties.set(false);
          }
        }),
        catchError(() => of([] as Property[])),
      );
    }),
    startWith([]),
    shareReplay(1),
  );

  typeRelationProperties = toSignal(this.typeRelationPropertiesForType$, { initialValue: [] as Property[] });

  setIdsForTypeRelationProperties(resourceTypeId: number, relTypeId: number, contextId: number) {
    this.typeRelationProperties$.next({ resourceTypeId, relTypeId, contextId });
  }

  setIdsForResourceTypeRelationTemplates(resourceTypeId: number, relationId: number) {
    this.resourceTypeRelationTemplates$.next({ resourceTypeId, relationId });
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

  bulkUpdateResourceRelationProperties(
    resourceId: number,
    relationId: number,
    updates: PropertyUpdate[],
    resets: PropertyUpdate[],
    contextId: number = 1,
  ): Observable<void> {
    const params = new HttpParams().set('contextId', contextId.toString());
    const body = { updates, resets };
    return this.http
      .put<void>(`${this.getBaseUrl()}/resources/${resourceId}/relations/${relationId}/properties`, body, {
        params,
        headers: this.getHeaders(),
      })
      .pipe(catchError(this.handleError));
  }

  bulkUpdateResourceTypeRelationProperties(
    resourceTypeId: number,
    relTypeId: number,
    updates: PropertyUpdate[],
    resets: PropertyUpdate[],
    contextId: number = 1,
  ): Observable<void> {
    const params = new HttpParams().set('contextId', contextId.toString());
    const body = { updates, resets };
    return this.http
      .put<void>(`${this.getBaseUrl()}/resourceTypes/${resourceTypeId}/relations/${relTypeId}/properties`, body, {
        params,
        headers: this.getHeaders(),
      })
      .pipe(catchError(this.handleError));
  }

  addResourceRelation(
    resourceId: number,
    slaveResourceGroupId: number,
    provided: boolean,
    relationName?: string,
  ): Observable<void> {
    const body: { slaveResourceGroupId: number; provided: boolean; relationName?: string } = {
      slaveResourceGroupId,
      provided,
    };
    if (relationName) {
      body.relationName = relationName;
    }
    return this.http
      .post<void>(`${this.getBaseUrl()}/resources/${resourceId}/relations`, body, {
        headers: this.getHeaders(),
      })
      .pipe(catchError(this.handleError));
  }

  removeResourceRelation(resourceId: number, relationId: number): Observable<void> {
    return this.http
      .delete<void>(`${this.getBaseUrl()}/resources/${resourceId}/relations/${relationId}`, {
        headers: this.getHeaders(),
      })
      .pipe(catchError(this.handleError));
  }

  addResourceTypeRelation(resourceTypeId: number, slaveResourceTypeId: number): Observable<void> {
    return this.http
      .post<void>(
        `${this.getBaseUrl()}/resourceTypes/${resourceTypeId}/relations`,
        { slaveResourceTypeId },
        { headers: this.getHeaders() },
      )
      .pipe(catchError(this.handleError));
  }

  removeResourceTypeRelation(resourceTypeId: number, relTypeId: number): Observable<void> {
    return this.http
      .delete<void>(`${this.getBaseUrl()}/resourceTypes/${resourceTypeId}/relations/${relTypeId}`, {
        headers: this.getHeaders(),
      })
      .pipe(catchError(this.handleError));
  }

  getResourceTypeRelationTemplates(resourceTypeId: number, relationId: number): Observable<ResourceTemplate[]> {
    return this.http
      .get<ResourceTemplate[]>(
        `${this.getBaseUrl()}/resourceTypes/${resourceTypeId}/relations/${relationId}/templates`,
        {
          headers: this.getHeaders(),
        },
      )
      .pipe(catchError(this.handleError));
  }

  getResourceRelationTemplates(resourceId: number, relationId: number): Observable<ResourceTemplate[]> {
    return this.http
      .get<ResourceTemplate[]>(`${this.getBaseUrl()}/resources/${resourceId}/relations/${relationId}/templates`, {
        headers: this.getHeaders(),
      })
      .pipe(catchError(this.handleError));
  }

  addResourceRelationTemplate(template: ResourceTemplate, resourceId: number, relationId: number) {
    return this.http
      .post<ResourceTemplate>(
        `${this.getBaseUrl()}/resources/${resourceId}/relations/${relationId}/addTemplate`,
        template,
        {
          headers: this.getHeaders(),
        },
      )
      .pipe(catchError(this.handleError));
  }

  addResourceTypeRelationTemplate(template: ResourceTemplate, resourceTypeId: number, relationId: number) {
    return this.http
      .post<ResourceTemplate>(
        `${this.getBaseUrl()}/resourceTypes/${resourceTypeId}/relations/${relationId}/addTemplate`,
        template,
        {
          headers: this.getHeaders(),
        },
      )
      .pipe(catchError(this.handleError));
  }

  updateResourceRelationTemplate(template: ResourceTemplate, resourceId: number, relationId: number) {
    return this.http
      .put<ResourceTemplate>(
        `${this.getBaseUrl()}/resources/${resourceId}/relations/${relationId}/updateTemplate`,
        template,
        {
          headers: this.getHeaders(),
        },
      )
      .pipe(catchError(this.handleError));
  }

  updateResourceTypeRelationTemplate(template: ResourceTemplate, resourceTypeId: number, relationId: number) {
    return this.http
      .put<ResourceTemplate>(
        `${this.getBaseUrl()}/resourceTypes/${resourceTypeId}/relations/${relationId}/updateTemplate`,
        template,
        {
          headers: this.getHeaders(),
        },
      )
      .pipe(catchError(this.handleError));
  }
}
