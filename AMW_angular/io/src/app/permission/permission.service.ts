import { Injectable } from '@angular/core';
import { Http, Response, URLSearchParams, RequestOptions } from '@angular/http';
import { Observable } from 'rxjs/Rx';
import { Permission } from './permission';
import { Restriction } from './restriction';
import { RestrictionsCreation } from './restrictions-creation';
import { BaseService } from '../base/base.service';

@Injectable()
export class PermissionService extends BaseService {

  constructor(private http: Http) {
    super();
  }

  getAllRoleNames(): Observable<string[]> {
    return this.http
      .get(`${this.getBaseUrl()}/permissions/restrictions/roleNames`, {headers: this.getHeaders()})
      .map(this.extractPayload)
      .catch(this.handleError);
  }

  getAllUserRestrictionNames(): Observable<string[]> {
    return this.http
      .get(`${this.getBaseUrl()}/permissions/restrictions/userRestrictionNames`, {headers: this.getHeaders()})
      .map(this.extractPayload)
      .catch(this.handleError);
  }

  getAllPermissionEnumValues(): Observable<Permission[]> {
    return this.http
      .get(`${this.getBaseUrl()}/permissions/restrictions/permissionEnumValues`, {headers: this.getHeaders()})
      .map(this.extractPayload)
      .catch(this.handleError);
  }

  getRoleWithRestrictions(roleName: string): Observable<Restriction[]> {
    return this.http
      .get(`${this.getBaseUrl()}/permissions/restrictions/roles/${roleName}`, {headers: this.getHeaders()})
      .map(this.extractPayload)
      .catch(this.handleError);
  }

  getUserWithRestrictions(userName: string): Observable<Restriction[]> {
    return this.http
      .get(`${this.getBaseUrl()}/permissions/restrictions/users/${userName}`, {headers: this.getHeaders()})
      .map(this.extractPayload)
      .catch(this.handleError);
  }

  getOwnUserAndRoleRestrictions(): Observable<Restriction[]> {
    return this.http
      .get(`${this.getBaseUrl()}/permissions/restrictions/ownRestrictions/`, {headers: this.getHeaders()})
      .map(this.extractPayload)
      .catch(this.handleError);
  }

  removeRestriction(id: number) {
    return this.http
      .delete(`${this.getBaseUrl()}/permissions/restrictions/${id}`, {headers: this.getHeaders()})
      .map(this.extractPayload)
      .catch(this.handleError);
  }

  updateRestriction(restriction: Restriction) {
    return this.http.put(`${this.getBaseUrl()}/permissions/restrictions/${restriction.id}`, restriction, {headers: this.postHeaders()})
      .map(this.extractPayload)
      .catch(this.handleError);
  }

  createRestriction(restriction: Restriction, delegation: boolean): Observable<Restriction> {
    const params: URLSearchParams = new URLSearchParams();
    params.set('delegation', delegation ? 'true' : 'false');
    const options = new RequestOptions({
      search: params,
      headers: this.postHeaders()
    });
    return this.http.post(`${this.getBaseUrl()}/permissions/restrictions/`, restriction, options)
      .flatMap((res: Response) => {
        return this.http.get(this.getBaseUrl() + res.headers.get('Location'));
      }).map(this.extractPayload)
      .catch(this.handleError);
  }

  createRestrictions(restrictionsCreation: RestrictionsCreation, delegation: boolean) {
    const params: URLSearchParams = new URLSearchParams();
    params.set('delegation', delegation ? 'true' : 'false');
    const options = new RequestOptions({
      search: params,
      headers: this.postHeaders()
    });
    return this.http.post(`${this.getBaseUrl()}/permissions/restrictions/multi/`, restrictionsCreation, options)
      .map(this.extractPayload)
      .catch(this.handleError);
  }

}

