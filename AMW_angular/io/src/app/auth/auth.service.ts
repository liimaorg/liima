import { computed, Injectable, Signal } from '@angular/core';
import { BaseService } from '../base/base.service';
import { HttpClient } from '@angular/common/http';
import { Observable, startWith, Subject } from 'rxjs';
import { catchError, shareReplay, switchMap } from 'rxjs/operators';
import { Restriction } from '../settings/permission/restriction';
import { toSignal } from '@angular/core/rxjs-interop';
import { DefaultResourceType } from './defaultResourceType';

@Injectable({ providedIn: 'root' })
export class AuthService extends BaseService {
  private reload$ = new Subject<Restriction[]>();
  private restrictions$ = this.reload$.pipe(
    startWith(null),
    switchMap(() => this.getRestrictions()),
    shareReplay(1),
  );
  restrictions = toSignal(this.restrictions$, { initialValue: [] as Restriction[] });

  constructor(private http: HttpClient) {
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
      this.getActionsForPermission(permissionName).find((value) => value === 'ALL' || value === action) !== undefined
    );
  }

  hasResourcePermission(permissionName: string, action: string, resourceType: string): boolean {
    return (
      this.restrictions()
        .filter((entry) => entry.permission.name === permissionName)
        .filter((entry) => entry.resourceTypeName === resourceType || this.isDefaultType(entry, resourceType))
        .map((entry) => entry.action)
        .find((entry) => entry === 'ALL' || entry === action) !== undefined
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
    return action === 'ALL' || action === role;
  };
}
