import { Injectable } from '@angular/core';
import { Http, Response, Headers } from '@angular/http';
import { Observable } from 'rxjs/Rx';
import { Environment } from './permission';
import { Restriction } from './restriction';

@Injectable()
export class PermissionService {
  private baseUrl: string = '/AMW_rest/resources';

  constructor(private http: Http) {
  }

  getAllRoleNames(): Observable<string[]> {
    let resource$ = this.http
      .get(`${this.baseUrl}/permissions/restrictions/roleNames`, {headers: this.getHeaders()})
      .map((response: Response) => response.json())
      .catch(handleError);
    return resource$;
  }

  getRoleWithRestrictions(roleName: string): Observable<Restriction[]> {
    let resource$ = this.http
      .get(`${this.baseUrl}/permissions/restrictions/roles/${roleName}`, {headers: this.getHeaders()})
      .map((response: Response) => response.json())
      .catch(handleError);
    return resource$;
  }

  getUserWithRestrictions(userName: string): Observable<Restriction[]> {
    let resource$ = this.http
      .get(`${this.baseUrl}/permissions/restrictions/users/${userName}`, {headers: this.getHeaders()})
      .map((response: Response) => response.json())
      .catch(handleError);
    return resource$;
  }

  removeRestriction(id: number) {
    let resource$ = this.http
      .delete(`${this.baseUrl}/permissions/restrictions/${id}`, {headers: this.getHeaders()})
      .map((response: Response) => response.json())
      .catch(handleError);
    return resource$;
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
}

// this could also be a private method of the component class
function handleError(error: any) {
  // log error
  // could be something more sofisticated
  let errorMsg = error.message || `Error retrieving your data`;
  console.error(errorMsg);

  // throw an application level error
  return Observable.throw(errorMsg);
}
