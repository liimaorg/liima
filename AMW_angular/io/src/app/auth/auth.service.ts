import { Injectable } from '@angular/core';
import { BaseService } from '../base/base.service';
import { HttpClient } from '@angular/common/http';
import { filter, Observable, Subject } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { Restriction } from '../settings/permission/restriction';
import { Release } from '../settings/releases/release';

@Injectable({ providedIn: 'root' })
export class AuthService extends BaseService {
  private readonly _cachedUserRestrictions: Subject<Restriction[]>;
  private readonly _userData: Observable<Restriction[]>;

  constructor(private http: HttpClient) {
    super();
    if (!this._cachedUserRestrictions) {
      this._cachedUserRestrictions = new Subject<Restriction[]>();
      this.refreshData();
    }
    this._userData = this._cachedUserRestrictions.asObservable();
  }

  private refreshData() {
    this.getRestrictions().subscribe({
      next: (r) => this._cachedUserRestrictions.next(r),
      error: (e) => console.log(e),
    });
  }

  private getRestrictions(): Observable<Restriction[]> {
    return this.http
      .get<Restriction[]>(`${this.getBaseUrl()}/permissions/restrictions/ownRestrictions/`, {
        headers: this.getHeaders(),
      })
      .pipe(catchError(this.handleError));
  }

  get userRestrictions() {
    return this._userData;
  }

  getActionsForPermission(permissionName: string) {
    return this._userData.pipe(
      map((restrictions) => {
        return restrictions.filter((entry) => entry.permission.name === permissionName).map((entry) => entry.action);
      }),
    );
  }
}
