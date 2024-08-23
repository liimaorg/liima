import { Injectable } from '@angular/core';
import { BaseService } from '../base/base.service';
import { HttpClient } from '@angular/common/http';
import { Observable, startWith, Subject } from 'rxjs';
import { catchError, shareReplay, switchMap } from 'rxjs/operators';
import { Restriction } from '../settings/permission/restriction';
import { toSignal } from '@angular/core/rxjs-interop';

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
}

// currying function which verifies roles in a action
export function isAllowed(role: string) {
  return (action: string) => {
    return action === 'ALL' || action === role;
  };
}
