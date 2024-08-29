import { inject, Injectable } from '@angular/core';
import { BaseService } from '../../base/base.service';
import { HttpClient } from '@angular/common/http';
import { Observable, startWith, Subject } from 'rxjs';
import { PropertyType } from './property-type';
import { catchError, shareReplay, switchMap } from 'rxjs/operators';
import { toSignal } from '@angular/core/rxjs-interop';
import { Restriction } from '../permission/restriction';

@Injectable({ providedIn: 'root' })
export class PropertyTypesService extends BaseService {
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
    return this.http
      .get<PropertyType[]>(`${this.getBaseUrl()}/settings/propertyTypes`)
      .pipe(catchError(this.handleError));
  }

  reload() {
    this.reload$.next([]);
  }
}
