import { Injectable } from '@angular/core';
import { Http, Response, Headers } from '@angular/http';
import { Observable } from 'rxjs/Rx';
import { Permission} from './permission';
import { Restriction } from './restriction';

@Injectable()
export class PermissionService {
  private baseUrl: string = '/AMW_rest/resources';

  constructor(private http: Http) {
  }

  getAllRoleNames(): Observable<string[]> {
    let resource$ = this.http
      .get(`${this.baseUrl}/permissions/restrictions/roleNames`, {headers: this.getHeaders()})
      .map(this.extractPayload)
      .catch(handleError);
    return resource$;
  }

  getAllUserRestrictionNames(): Observable<string[]> {
    let resource$ = this.http
      .get(`${this.baseUrl}/permissions/restrictions/userRestrictionNames`, {headers: this.getHeaders()})
      .map(this.extractPayload)
      .catch(handleError);
    return resource$;
  }

  getAllPermissionEnumValues(): Observable<Permission[]> {
    let resource$ = this.http
      .get(`${this.baseUrl}/permissions/restrictions/permissionEnumValues`, {headers: this.getHeaders()})
      .map(this.extractPayload)
      .catch(handleError);
    return resource$;
  }

  getRoleWithRestrictions(roleName: string): Observable<Restriction[]> {
    let resource$ = this.http
      .get(`${this.baseUrl}/permissions/restrictions/roles/${roleName}`, {headers: this.getHeaders()})
      .map(this.extractPayload)
      .catch(handleError);
    return resource$;
  }

  getUserWithRestrictions(userName: string): Observable<Restriction[]> {
    let resource$ = this.http
      .get(`${this.baseUrl}/permissions/restrictions/users/${userName}`, {headers: this.getHeaders()})
      .map(this.extractPayload)
      .catch(handleError);
    return resource$;
  }

  removeRestriction(id: number) {
    let resource$ = this.http
      .delete(`${this.baseUrl}/permissions/restrictions/${id}`, {headers: this.getHeaders()})
      .map(this.extractPayload)
      .catch(handleError);
    return resource$;
  }

  updateRestriction(restriction: Restriction) {
    return this.http.put(`${this.baseUrl}/permissions/restrictions/${restriction.id}`, restriction, {headers: this.postHeaders()})
      .map(this.extractPayload)
      .catch(handleError);
  }

  createRestriction(restriction: Restriction): Observable<Restriction> {
    return this.http.post(`${this.baseUrl}/permissions/restrictions/`, restriction, {headers: this.postHeaders()})
      .flatMap((res: Response) => {
        // var location = res.headers.get('Location');
        return this.http.get(this.baseUrl + res.headers.get('Location'));
      }).map(this.extractPayload)
      .catch(handleError);
  }

  private getHeaders() {
    let headers = new Headers();
    headers.append('Accept', 'application/json');
    return headers;
  }

  private postHeaders() {
    let headers = new Headers();
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
  // log error
  // could be something more sofisticate
  let errorMsg = error.message || `Error retrieving your data`;
  console.error(errorMsg);

  // throw an application level error
  return Observable.throw(errorMsg);
}
