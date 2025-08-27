import { computed, inject, Injectable, Signal } from '@angular/core';
import { BaseService } from '../base/base.service';
import { HttpClient } from '@angular/common/http';
import { Observable, startWith, Subject } from 'rxjs';
import { catchError, shareReplay, switchMap } from 'rxjs/operators';
import { Action, Restriction } from 'src/app/auth/restriction';
import { toSignal } from '@angular/core/rxjs-interop';
import { DefaultResourceType } from './defaultResourceType';
import { EnvironmentService } from '../deployment/environment.service';
import { Environment } from '../deployment/environment';

@Injectable({ providedIn: 'root' })
export class AuthService extends BaseService {
  private http = inject(HttpClient);
  private environmentsService = inject(EnvironmentService);
  private reload$ = new Subject<Restriction[]>();
  private restrictions$ = this.reload$.pipe(
    startWith(null),
    switchMap(() => this.getOwnRestrictions()),
    shareReplay(1),
  );
  restrictions = toSignal(this.restrictions$, { initialValue: [] as Restriction[] });
  environments: Signal<{ [name: string]: Environment }> = computed(() => {
    return this.environmentsService.contexts().reduce((acc, env) => {
      acc[env.name] = env;
      return acc;
    }, {});
  });

  constructor() {
    super();
  }

  refreshData() {
    this.reload$.next([]);
  }

  private getOwnRestrictions(): Observable<Restriction[]> {
    return this.http
      .get<Restriction[]>(`${this.getBaseUrl()}/permissions/restrictions/ownRestrictions/`, {
        headers: this.getHeaders(),
      })
      .pipe(catchError(this.handleError));
  }

  getActionsForPermission(permissionName: string): string[] {
    return this.restrictions()
      .filter((entry) => entry.permission.name === permissionName)
      .map((entry) => entry.action);
  }

  hasPermission(
    permissionName: string,
    action: Action,
    resourceTypeName: string = null,
    resourceGroupId: number = null,
    context: string = null,
  ): boolean {
    return (
      this.restrictions()
        .filter((entry) => entry.permission.name === permissionName)
        .filter(
          (entry) =>
            entry.resourceTypePermission === 'ANY' ||
            (entry.resourceTypePermission === 'DEFAULT_ONLY' &&
              Object.keys(DefaultResourceType).includes(resourceTypeName)) ||
            (entry.resourceTypePermission === 'NON_DEFAULT_ONLY' &&
              !Object.keys(DefaultResourceType).includes(resourceTypeName)),
        )
        .filter((entry) => entry.resourceTypeName === null || entry.resourceTypeName === resourceTypeName)
        .filter((entry) => entry.resourceGroupId === null || entry.resourceGroupId === resourceGroupId)
        .filter((entry) => this.hasContextPermission(entry, context))
        .find((entry) => entry.action === 'ALL' || entry.action === action) !== undefined
    );
  }

  private hasContextPermission(entry: Restriction, context: string): boolean {
    if (context === null || entry.contextName === null || entry.contextName === 'GLOBAL') {
      return true;
    }
    if (entry.contextName === context) {
      return true;
    }
    // only three levels possible, GLOBAL and env has already been checked
    if (entry.contextName === this.environments()[context].parentName) {
      return true;
    }
  }
}

// curried function to verify a role in an action
// usage example: actions.some(isAllowed("CREATE"))
export function isAllowed(role: string) {
  return (action: string) => {
    return action === 'ALL' || action === role;
  };
}
