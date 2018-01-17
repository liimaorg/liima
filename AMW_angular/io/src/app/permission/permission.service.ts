import { Injectable } from '@angular/core';
import { Http, Response, Headers, URLSearchParams, RequestOptions } from '@angular/http';
import { Observable } from 'rxjs/Rx';
import { Permission } from './permission';
import { Restriction } from './restriction';
import { RestrictionsCreation } from './restrictions-creation';
import * as _ from 'lodash';

@Injectable()
export class PermissionService {
  private baseUrl: string = '/AMW_rest/resources';

  constructor(private http: Http) {
  }

  getAllRoleNames(): Observable<string[]> {
    return this.http
      .get(`${this.baseUrl}/permissions/restrictions/roleNames`, {headers: this.getHeaders()})
      .map(this.extractPayload)
      .catch(handleError);
  }

  getAllUserRestrictionNames(): Observable<string[]> {
    return this.http
      .get(`${this.baseUrl}/permissions/restrictions/userRestrictionNames`, {headers: this.getHeaders()})
      .map(this.extractPayload)
      .catch(handleError);
  }

  getAllPermissionEnumValues(): Observable<Permission[]> {
    return this.http
      .get(`${this.baseUrl}/permissions/restrictions/permissionEnumValues`, {headers: this.getHeaders()})
      .map(this.extractPayload)
      .catch(handleError);
  }

  getRoleWithRestrictions(roleName: string): Observable<Restriction[]> {
    return this.http
      .get(`${this.baseUrl}/permissions/restrictions/roles/${roleName}`, {headers: this.getHeaders()})
      .map(this.extractPayload)
      .catch(handleError);
  }

  getUserWithRestrictions(userName: string): Observable<Restriction[]> {
    return this.http
      .get(`${this.baseUrl}/permissions/restrictions/users/${userName}`, {headers: this.getHeaders()})
      .map(this.extractPayload)
      .catch(handleError);
  }

  getOwnUserAndRoleRestrictions(): Observable<Restriction[]> {
    return this.http
      .get(`${this.baseUrl}/permissions/restrictions/ownRestrictions/`, {headers: this.getHeaders()})
      .map(this.extractPayload)
      .catch(handleError);
  }

  removeRestriction(id: number) {
    return this.http
      .delete(`${this.baseUrl}/permissions/restrictions/${id}`, {headers: this.getHeaders()})
      .map(this.extractPayload)
      .catch(handleError);
  }

  updateRestriction(restriction: Restriction) {
    return this.http.put(`${this.baseUrl}/permissions/restrictions/${restriction.id}`, restriction, {headers: this.postHeaders()})
      .map(this.extractPayload)
      .catch(handleError);
  }

  createRestriction(restriction: Restriction, delegation: boolean): Observable<Restriction> {
    const params: URLSearchParams = new URLSearchParams();
    params.set('delegation', delegation ? 'true' : 'false');
    const options = new RequestOptions({
      search: params,
      headers: this.postHeaders()
    });
    return this.http.post(`${this.baseUrl}/permissions/restrictions/`, restriction, options)
      .flatMap((res: Response) => {
        return this.http.get(this.baseUrl + res.headers.get('Location'));
      }).map(this.extractPayload)
      .catch(handleError);
  }

  createRestrictions(restrictionsCreation: RestrictionsCreation, delegation: boolean) {
    const params: URLSearchParams = new URLSearchParams();
    params.set('delegation', delegation ? 'true' : 'false');
    const options = new RequestOptions({
      search: params,
      headers: this.postHeaders()
    });
    return this.http.post(`${this.baseUrl}/permissions/restrictions/multi/`, restrictionsCreation, options)
      .map(this.extractPayload)
      .catch(handleError);
  }

  private getHeaders() {
    const headers = new Headers();
    headers.append('Accept', 'application/json');
    return headers;
  }

  private postHeaders() {
    const headers = new Headers();
    headers.append('Content-Type', 'application/json');
    headers.append('Accept', 'application/json');
    return headers;
  }

  // to json without throwing an error if response is empty
  private extractPayload(res: Response) {
    return res.text() ? res.json() : {};
  }
}

// this could also be a private method of the component class
function handleError(error: any) {
  let errorMsg = 'Error retrieving your data';
  if (error._body) {
    try {
      errorMsg = _.escape(JSON.parse(error._body).message);
    } catch (e) {
      console.log(e);
    }
  }
  console.error(errorMsg);
  // throw an application level error
  return Observable.throw(errorMsg);
}
