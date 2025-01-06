import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { ResourceTemplate } from './resource-template';
import { catchError, shareReplay, switchMap } from 'rxjs/operators';
import { Observable, Subject } from 'rxjs';
import { BaseService } from '../base/base.service';
import { toSignal } from '@angular/core/rxjs-interop';

@Injectable({ providedIn: 'root' })
export class ResourceTemplatesService extends BaseService {
  private templates$: Subject<number> = new Subject<number>();

  private templateById$: Observable<ResourceTemplate[]> = this.templates$.pipe(
    switchMap((id: number) => this.getResourceTemplates(id)),
    shareReplay(1),
  );

  resourceTemplates = toSignal(this.templateById$, { initialValue: [] });

  constructor(private http: HttpClient) {
    super();
  }

  setIdForResourceTemplateList(id: number) {
    this.templates$.next(id);
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
}
