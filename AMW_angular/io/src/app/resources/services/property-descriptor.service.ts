import { inject, Injectable, signal } from '@angular/core';
import { BaseService } from '../../base/base.service';
import { HttpClient } from '@angular/common/http';
import { Observable, Subject } from 'rxjs';
import { catchError, finalize, shareReplay, startWith, switchMap } from 'rxjs/operators';
import { PropertyDescriptor } from '../models/property-descriptor';
import { toSignal } from '@angular/core/rxjs-interop';

@Injectable({ providedIn: 'root' })
export class PropertyDescriptorService extends BaseService {
  private url = `${this.getBaseUrl()}/propertyDescriptors`;
  private http = inject(HttpClient);

  private loadingDescriptor = signal(false);
  isLoadingDescriptor = this.loadingDescriptor.asReadonly();

  private descriptorId$ = new Subject<number>();

  private descriptor$ = this.descriptorId$.pipe(
    switchMap((id) => {
      this.loadingDescriptor.set(true);
      return this.fetchPropertyDescriptor(id).pipe(finalize(() => this.loadingDescriptor.set(false)));
    }),
    startWith(null),
    shareReplay(1),
  );

  propertyDescriptor = toSignal(this.descriptor$, { initialValue: null as PropertyDescriptor | null });

  loadPropertyDescriptor(descriptorId: number): void {
    this.descriptorId$.next(descriptorId);
  }

  save(descriptor: PropertyDescriptor, resourceId?: number, resourceTypeId?: number): Observable<PropertyDescriptor> {
    if (descriptor.id) {
      return this.update(descriptor, resourceId, resourceTypeId);
    } else {
      return this.create(descriptor, resourceId, resourceTypeId);
    }
  }

  delete(descriptorId: number, resourceId?: number, resourceTypeId?: number): Observable<void> {
    const params = new URLSearchParams();
    if (resourceId) {
      params.append('resourceId', resourceId.toString());
    }
    if (resourceTypeId) {
      params.append('resourceTypeId', resourceTypeId.toString());
    }

    const url = params.toString() ? `${this.url}/${descriptorId}?${params.toString()}` : `${this.url}/${descriptorId}`;

    return this.http
      .delete<void>(url, {
        headers: this.getHeaders(),
      })
      .pipe(catchError(this.handleError));
  }

  forceDelete(descriptorId: number, resourceId?: number, resourceTypeId?: number): Observable<void> {
    const params = new URLSearchParams();
    params.append('force', 'true');
    if (resourceId) {
      params.append('resourceId', resourceId.toString());
    }
    if (resourceTypeId) {
      params.append('resourceTypeId', resourceTypeId.toString());
    }

    return this.http
      .delete<void>(`${this.url}/${descriptorId}?${params.toString()}`, {
        headers: this.getHeaders(),
      })
      .pipe(catchError(this.handleError));
  }

  private fetchPropertyDescriptor(descriptorId: number): Observable<PropertyDescriptor> {
    return this.http
      .get<PropertyDescriptor>(`${this.url}/${descriptorId}`, {
        headers: this.getHeaders(),
      })
      .pipe(catchError(this.handleError));
  }

  private create(
    descriptor: PropertyDescriptor,
    resourceId?: number,
    resourceTypeId?: number,
  ): Observable<PropertyDescriptor> {
    const params = new URLSearchParams();
    if (resourceId) {
      params.append('resourceId', resourceId.toString());
    }
    if (resourceTypeId) {
      params.append('resourceTypeId', resourceTypeId.toString());
    }

    const url = params.toString() ? `${this.url}?${params.toString()}` : this.url;

    return this.http
      .post<PropertyDescriptor>(url, descriptor, {
        headers: this.getHeaders(),
      })
      .pipe(catchError(this.handleError));
  }

  private update(
    descriptor: PropertyDescriptor,
    resourceId?: number,
    resourceTypeId?: number,
  ): Observable<PropertyDescriptor> {
    const params = new URLSearchParams();
    if (resourceId) {
      params.append('resourceId', resourceId.toString());
    }
    if (resourceTypeId) {
      params.append('resourceTypeId', resourceTypeId.toString());
    }

    const url = params.toString()
      ? `${this.url}/${descriptor.id}?${params.toString()}`
      : `${this.url}/${descriptor.id}`;

    return this.http
      .put<PropertyDescriptor>(url, descriptor, {
        headers: this.getHeaders(),
      })
      .pipe(catchError(this.handleError));
  }
}
