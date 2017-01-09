import { Injectable } from '@angular/core';
import { Http, Response, Headers } from '@angular/http';
import { Observable } from 'rxjs/Rx';
import { Deployment } from './deployment';

@Injectable()
export class DeploymentService {
  private baseUrl: string = 'http://localhost:8080/AMW_rest/resources';

  constructor(private http: Http) {
  }

  getAll(): Observable<Deployment[]> {
    let resource$ = this.http
      .get(`${this.baseUrl}/deployments`, {headers: this.getHeaders()})
      .map((response: Response) => response.json())
      .catch(handleError);
    return resource$;
  }

  private getHeaders() {
    let headers = new Headers();
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
