import { Injectable } from '@angular/core';
import { BaseService } from '../base/base.service';
import { HttpClient } from '@angular/common/http';
import { Observable, startWith, Subject } from 'rxjs';
import { catchError, map, shareReplay, switchMap } from 'rxjs/operators';
import { Restriction } from '../settings/permission/restriction';

@Injectable({ providedIn: 'root' })
export class AuthService extends BaseService {
  private reload$ = new Subject<Restriction[]>();
  private readonly restrictions$ = this.reload$.pipe(
    startWith(null),
    switchMap(() => this.getRestrictions()),
    shareReplay(1),
  );

  constructor(private http: HttpClient) {
    super();
  }

  refreshData() {
    this.getRestrictions().pipe(map((v) => this.reload$.next(v)));
  }

  private getRestrictions(): Observable<Restriction[]> {
    return this.http
      .get<Restriction[]>(`${this.getBaseUrl()}/permissions/restrictions/ownRestrictions/`, {
        headers: this.getHeaders(),
      })
      .pipe(catchError(this.handleError));
  }

  get userRestrictions() {
    return this.restrictions$;
  }

  getActionsForPermission(permissionName: string): Observable<string[]> {
    return this.restrictions$.pipe(
      map((restrictions) => {
        return restrictions.filter((entry) => entry.permission.name === permissionName).map((entry) => entry.action);
      }),
    );
  }

  hasPermission(permissionName: string, action: string): Observable<boolean> {
    return this.getActionsForPermission(permissionName).pipe(map(values => {
      return values.find(value => value === 'ALL' || value === action) !== undefined;
    })
    )
  }

}
