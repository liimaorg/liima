import { Injectable } from '@angular/core';
import { BaseService } from '../base/base.service';
import { HttpClient } from '@angular/common/http';
import { Observable, Subject } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { Restriction } from '../settings/permission/restriction';

@Injectable({ providedIn: 'root' })
export class AuthService extends BaseService {
  private readonly _cachedUserRestrictions: Subject<Restriction[]>;
  private readonly _userData;

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
}
