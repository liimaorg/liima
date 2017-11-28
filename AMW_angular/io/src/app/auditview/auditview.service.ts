import { Injectable } from '@angular/core';
import { Http, Response, Headers, RequestOptions, URLSearchParams } from '@angular/http';
import { Observable } from 'rxjs/Rx';

@Injectable()
export class AuditviewService {
  private baseUrl: string = '/AMW_rest/auditview';

  constructor(private http: Http) {
  }

}
