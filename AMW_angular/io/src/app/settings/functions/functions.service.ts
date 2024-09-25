import { Injectable } from '@angular/core';
import { BaseService } from '../../base/base.service';
import { HttpClient } from '@angular/common/http';
import { Function } from './function';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class FunctionsService extends BaseService {
  constructor(private http: HttpClient) {
    super();
  }

  getAllFunctions(): Observable<Function[]> {
    return this.http.get<Function[]>(`${this.getBaseUrl()}/functions`);
  }

  addFunction(newFunction: Function): Observable<any> {
    return this.http.post(`${this.getBaseUrl()}/functions`, newFunction);
  }

  deleteFunction(id: number): Observable<any> {
    return this.http.delete(`${this.getBaseUrl()}/functions/${id}`);
  }
}
