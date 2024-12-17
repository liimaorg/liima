import { BaseService } from '../../../base/base.service';
import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { ResourceFunction } from './resource-function';
import { catchError } from 'rxjs/operators';

@Injectable({ providedIn: 'root' })
export class ResourceFunctionsService extends BaseService {
  private path = `${this.getBaseUrl()}/resources/functions`;

  functions = signal<ResourceFunction[]>([]);

  constructor(private http: HttpClient) {
    super();
  }

  getResourceFunctions(id: number): void {
    this.http
      .get<ResourceFunction[]>(`${this.path}/${id}`, {
        headers: this.getHeaders(),
      })
      .pipe(catchError(this.handleError))
      .subscribe((result) => this.functions.set(result));
  }
}
