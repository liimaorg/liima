import { HttpErrorResponse, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { throwError } from 'rxjs';

@Injectable()
export class BaseService {
  private baseUrl = '/AMW_rest/resources';

  public getBaseUrl(): string {
    return this.baseUrl;
  }

  public getHeaders(): HttpHeaders {
    return new HttpHeaders().append('Accept', 'application/json');
  }

  public postHeaders() {
    let headers = new HttpHeaders();
    headers = headers.append('Content-Type', 'application/json');
    headers = headers.append('Accept', 'application/json');
    return headers;
  }

  public handleError(response: HttpErrorResponse) {
    // Return the full error response so services can check status codes and handle appropriately
    return throwError(() => response);
  }
}
