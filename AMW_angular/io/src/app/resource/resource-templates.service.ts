import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { ResourceTemplate } from './resource-template';
import { catchError, shareReplay, switchMap } from 'rxjs/operators';
import { Observable, Subject } from 'rxjs';
import { BaseService } from '../base/base.service';
import { toSignal } from '@angular/core/rxjs-interop';
import { RevisionInformation } from '../shared/model/revisionInformation';
@Injectable({ providedIn: 'root' })
export class ResourceTemplatesService extends BaseService {
  private templates$: Subject<number> = new Subject<number>();
  private templatesForType$: Subject<number> = new Subject<number>();
  private contextIdForAllTargetPlatforms$: Subject<number> = new Subject<number>();

  private templateById$: Observable<ResourceTemplate[]> = this.templates$.pipe(
    switchMap((id: number) => this.getResourceTemplates(id)),
    shareReplay(1),
  );

  private templateByTypeId$: Observable<ResourceTemplate[]> = this.templatesForType$.pipe(
    switchMap((id: number) => this.getResourceTypeTemplates(id)),
    shareReplay(1),
  );

  resourceTemplates = toSignal(this.templateById$, { initialValue: [] });

  resourceTypeTemplates = toSignal(this.templateByTypeId$, { initialValue: [] });

  constructor(private http: HttpClient) {
    super();
  }

  setIdForResourceTemplateList(id: number) {
    this.templates$.next(id);
  }

  setIdForResourceTypeTemplateList(id: number) {
    this.templatesForType$.next(id);
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

  deleteTemplate(id: number): Observable<void> {
    return this.http
      .delete<void>(`${this.getBaseUrl()}/resources/templates/${id}`, {
        headers: this.getHeaders(),
      })
      .pipe(catchError(this.handleError));
  }

  updateResourceTemplate(template: ResourceTemplate, resourceId: number) {
    return this.http
      .put<ResourceTemplate>(`${this.getBaseUrl()}/resources/templates/updateForResource/${resourceId}`, template, {
        headers: this.getHeaders(),
      })
      .pipe(catchError(this.handleError));
  }

  updateResourceTypeTemplate(template: ResourceTemplate, resourceId: number) {
    return this.http
      .put<ResourceTemplate>(`${this.getBaseUrl()}/resources/templates/updateForResourceType/${resourceId}`, template, {
        headers: this.getHeaders(),
      })
      .pipe(catchError(this.handleError));
  }

  addResourceTemplate(template: ResourceTemplate, resourceId: number) {
    return this.http
      .post<ResourceTemplate>(`${this.getBaseUrl()}/resources/templates/addForResource/${resourceId}`, template, {
        headers: this.getHeaders(),
      })
      .pipe(catchError(this.handleError));
  }

  addResourceTypeTemplate(template: ResourceTemplate, resourceId: number) {
    return this.http
      .post<ResourceTemplate>(`${this.getBaseUrl()}/resources/templates/addForResourceType/${resourceId}`, template, {
        headers: this.getHeaders(),
      })
      .pipe(catchError(this.handleError));
  }

  getAllTargetPlatforms() {
    return this.http
      .get<string[]>(`${this.getBaseUrl()}/resources/templates/targetPlatforms`, {
        headers: this.getHeaders(),
      })
      .pipe(catchError(this.handleError));
  }

  getTemplateRevisions(id: number) {
    return this.http
      .get<RevisionInformation[]>(`${this.getBaseUrl()}/resources/templates/${id}/revisions`, {
        headers: this.getHeaders(),
      })
      .pipe(catchError(this.handleError));
  }

  getTemplateByIdAndRevision(id: number, revisionId: number): Observable<ResourceTemplate> {
    return this.http
      .get<ResourceTemplate>(`${this.getBaseUrl()}/resources/templates/${id}/revisions/${revisionId}`, {
        headers: this.getHeaders(),
      })
      .pipe(catchError(this.handleError));
  }
}
