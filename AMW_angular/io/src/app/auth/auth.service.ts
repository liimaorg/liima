import { inject, Injectable } from '@angular/core';
import { BaseService } from '../base/base.service';
import { HttpClient } from '@angular/common/http';
import { Observable, startWith, Subject } from 'rxjs';
import { catchError, shareReplay, switchMap } from 'rxjs/operators';
import { Restriction } from '../settings/permission/restriction';
import { toSignal } from '@angular/core/rxjs-interop';
import { DefaultResourceType } from './defaultResourceType';

export enum Action {
  READ = 'READ',
  CREATE = 'CREATE',
  UPDATE = 'UPDATE',
  DELETE = 'DELETE',
  ALL = 'ALL',
}

@Injectable({ providedIn: 'root' })
export class AuthService extends BaseService {
  private http = inject(HttpClient);
  private reload$ = new Subject<Restriction[]>();
  private restrictions$ = this.reload$.pipe(
    startWith(null),
    switchMap(() => this.getRestrictions()),
    shareReplay(1),
  );
  restrictions = toSignal(this.restrictions$, { initialValue: [] as Restriction[] });

  constructor() {
    super();
  }

  refreshData() {
    this.reload$.next([]);
  }

  private getRestrictions(): Observable<Restriction[]> {
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

  hasPermission(permissionName: string, action: string): boolean {
    return (
      this.getActionsForPermission(permissionName).find((value) => value === Action.ALL || value === action) !==
      undefined
    );
  }

  hasResourceGroupPermission(permissionName: string, action: string, resourceGroupId: number): boolean {
    return (
      this.restrictions()
        .filter((entry) => entry.permission.name === permissionName)
        .filter((entry) => entry.resourceGroupId === resourceGroupId || entry.resourceGroupId === null)
        .map((entry) => entry.action)
        .find((entry) => entry === Action.ALL || entry === action) !== undefined
    );
  }

  hasResourceTypePermission(permissionName: string, action: string, resourceTypeName: string): boolean {
    return (
      this.restrictions()
        .filter((entry) => entry.permission.name === permissionName)
        .filter(
          (entry) =>
            entry.resourceTypeName === resourceTypeName ||
            this.isDefaultType(entry, resourceTypeName) ||
            entry.resourceTypeName === null,
        )
        .map((entry) => entry.action)
        .find((entry) => entry === Action.ALL || entry === action) !== undefined
    );
  }

  private isDefaultType(entry: Restriction, resourceType: string) {
    if (entry.resourceTypeName === null && entry.resourceTypePermission === 'DEFAULT_ONLY') {
      return Object.keys(DefaultResourceType).find((key) => key === resourceType);
    } else return false;
  }
}

// curried function to verify a role in an action
// usage example: actions.some(isAllowed("CREATE"))
export function isAllowed(role: string) {
  return (action: string) => {
    return action === Action.ALL || action === role;
  };
}
