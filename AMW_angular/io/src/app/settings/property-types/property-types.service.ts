import { inject, Injectable } from '@angular/core';
import { BaseService } from '../../base/base.service';
import { HttpClient } from '@angular/common/http';
import { Observable, startWith, Subject } from 'rxjs';
import { PropertyType } from './property-type';
import { catchError, shareReplay, switchMap } from 'rxjs/operators';
import { toSignal } from '@angular/core/rxjs-interop';

@Injectable({ providedIn: 'root' })
export class PropertyTypesService extends BaseService {
  private url = `${this.getBaseUrl()}/settings/propertyTypes`;
  private http = inject(HttpClient);
  private reload$ = new Subject<PropertyType[]>();
  private propertyTypes$ = this.reload$.pipe(
    startWith(null),
    switchMap(() => this.fetchPropertyTypes()),
    shareReplay(1),
  );
  propertyTypes = toSignal(this.propertyTypes$, { initialValue: [] as PropertyType[] });

  constructor() {
    super();
  }

  private fetchPropertyTypes(): Observable<PropertyType[]> {
    return this.http.get<PropertyType[]>(this.url).pipe(catchError(this.handleError));
  }

  reload() {
    this.reload$.next([]);
  }

  save(propertyType: PropertyType) {
    if (propertyType.id) {
      return this.update(propertyType);
    } else {
      return this.create(propertyType);
    }
  }

  delete(id: number) {
    return this.http
      .delete<number>(`${this.url}/${id}`, {
        headers: this.getHeaders(),
      })
      .pipe(catchError(this.handleError));
  }

  private create(propertyType: PropertyType) {
    return this.http
      .post<PropertyType>(this.url, propertyType, {
        headers: this.getHeaders(),
      })
      .pipe(catchError(this.handleError));
  }

  private update(propertyType: PropertyType) {
    return this.http
      .put<PropertyType>(`${this.url}/${propertyType.id}`, propertyType, {
        headers: this.getHeaders(),
      })
      .pipe(catchError(this.handleError));
  }
}
