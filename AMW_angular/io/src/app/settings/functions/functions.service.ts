import { Injectable } from '@angular/core';
import { BaseService } from '../../base/base.service';
import { HttpClient } from '@angular/common/http';
import { catchError, map } from 'rxjs/operators';
import { Function } from './function';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class FunctionsService extends BaseService {
  constructor(private http: HttpClient) {
    super();
  }

  getAllFunctions(): Observable<Function[]> {
    return this.http.get<Function[]>(`${this.getBaseUrl()}/functions`).pipe(catchError(this.handleError));
  }

  getFunctionById(id: number): Observable<Function> {
    return this.http.get<Function>(`${this.getBaseUrl()}/functions/${id}`);
  }

  addFunction(newFunction: Function): Observable<any> {
    return this.http.post(`${this.getBaseUrl()}/functions`, newFunction);
  }

  deleteFunction(id: number): Observable<any> {
    return this.http.delete(`${this.getBaseUrl()}/functions/${id}`);
  }

  getCount(): Observable<number> {
    return this.getAllFunctions().pipe(
      map((functions) => functions.length),
      catchError(this.handleError),
    );
  }
}
