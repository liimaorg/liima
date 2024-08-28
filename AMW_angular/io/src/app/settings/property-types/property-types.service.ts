import { inject, Injectable } from '@angular/core';
import { BaseService } from '../../base/base.service';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { PropertyType } from './property-type';
import { catchError } from 'rxjs/operators';
import { toSignal } from '@angular/core/rxjs-interop';

@Injectable({ providedIn: 'root' })
export class PropertyTypesService extends BaseService {
  private http = inject(HttpClient);

  private propertyTypes$ = this.fetchPropertyTypes();
  propertyTypes = toSignal(this.propertyTypes$, { initialValue: [] as PropertyType[] });

  constructor() {
    super();
  }

  private fetchPropertyTypes(): Observable<PropertyType[]> {
    return this.http
      .get<PropertyType[]>(`${this.getBaseUrl()}/settings/propertyTypes`)
      .pipe(catchError(this.handleError));
  }
}
