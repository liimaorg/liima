import { Injectable } from '@angular/core';
import { BaseService } from '../base/base.service';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { Restriction } from '../settings/permission/restriction';

@Injectable({ providedIn: 'root' })
export class AuthService extends BaseService {
  private readonly _cachedUserRestrictions = new BehaviorSubject<Restriction[]>([]);

  private readonly _userData: Observable<Restriction[]>;

  constructor(private http: HttpClient) {
    super();
    this.refreshData();
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

  get userRestrictions(): Observable<Restriction[]> {
    return this._cachedUserRestrictions.asObservable();
  }

  getActionsForPermission(permissionName: string) {
    return this._userData.pipe(
      map((restrictions) => {
        return restrictions.filter((entry) => entry.permission.name === permissionName).map((entry) => entry.action);
      }),
    );
  }
}
