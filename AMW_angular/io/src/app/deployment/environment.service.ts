import { Injectable, Signal, inject, computed } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, startWith, Subject } from 'rxjs';
import { catchError, shareReplay, switchMap } from 'rxjs/operators';
import { Environment, EnvironmentTree } from './environment';
import { BaseService } from '../base/base.service';
import { toSignal } from '@angular/core/rxjs-interop';

@Injectable({ providedIn: 'root' })
export class EnvironmentService extends BaseService {
  private http = inject(HttpClient);

  private reload$ = new Subject<Environment[]>();
  private reloadedContexts = this.reload$.pipe(
    startWith(null),
    switchMap(() => this.getContexts()),
    shareReplay(1),
  );
  private reloadedEnvs = this.reload$.pipe(
    startWith(null),
    switchMap(() => this.getAll()),
    shareReplay(1),
  );
  contexts: Signal<Environment[]> = toSignal(this.reloadedContexts, { initialValue: [] as Environment[] });
  envs: Signal<Environment[]> = toSignal(this.reloadedEnvs, { initialValue: [] as Environment[] });
  environmentTree: Signal<EnvironmentTree[]> = computed(() => this.buildEnvironmentTree(this.contexts()));

  getAll(): Observable<Environment[]> {
    return this.getEnvironments(false);
  }

  getAllIncludingGroups(): Observable<Environment[]> {
    return this.getEnvironments(true);
  }

  private getEnvironments(includingGroups: boolean): Observable<Environment[]> {
    let params = new HttpParams();
    const headers = this.getHeaders();

    if (includingGroups) {
      params = params.set('includingGroups', 'true');
    }
    return this.http
      .get<Environment[]>(`${this.getBaseUrl()}/environments`, {
        params,
        headers,
      })
      .pipe(catchError(this.handleError));
  }

  getContexts(): Observable<Environment[]> {
    return this.http
      .get<Environment[]>(`${this.getBaseUrl()}/environments/contexts`)
      .pipe(catchError(this.handleError));
  }

  private buildEnvironmentTree(environments: Environment[], parentName: string | null = null): EnvironmentTree[] {
    return environments
      .filter((environment) => environment.parentName === parentName)
      .map((environment) => {
        return {
          id: environment.id,
          name: environment.name,
          nameAlias: environment.nameAlias,
          parentName: environment.parentName,
          parentId: environment.parentId,
          children: this.buildEnvironmentTree(environments, environment.name),
          selected: environment.selected,
          disabled: environment.disabled,
        };
      });
  }

  save(environment: Environment) {
    if (environment.id) return this.update(environment);
    return this.create(environment);
  }

  private create(environment: Environment) {
    return this.http
      .post<Environment>(`${this.getBaseUrl()}/environments/contexts`, environment, {
        headers: this.getHeaders(),
      })
      .pipe(catchError(this.handleError));
  }

  private update(environment: Environment) {
    return this.http
      .put<Environment>(`${this.getBaseUrl()}/environments/contexts/${environment.id}`, environment, {
        headers: this.getHeaders(),
      })
      .pipe(catchError(this.handleError));
  }

  delete(id: number) {
    return this.http
      .delete<number>(`${this.getBaseUrl()}/environments/contexts/${id}`, {
        headers: this.getHeaders(),
      })
      .pipe(catchError(this.handleError));
  }

  refreshData() {
    this.reload$.next([]);
  }
}
