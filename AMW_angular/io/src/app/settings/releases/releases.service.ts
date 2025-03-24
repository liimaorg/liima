import { Injectable } from '@angular/core';
import { BaseService } from '../../base/base.service';
import { HttpClient } from '@angular/common/http';
import { map, catchError } from 'rxjs/operators';
import { Release } from './release';
import { Observable } from 'rxjs';
import { ResourceEntity } from './resource-entity';
import { toSignal } from '@angular/core/rxjs-interop';

@Injectable({ providedIn: 'root' })
export class ReleasesService extends BaseService {
  private allReleases$ = this.getAllReleases();
  allReleases = toSignal(this.allReleases$, { initialValue: [] as Release[] });

  constructor(private http: HttpClient) {
    super();
  }

  getCount(): Observable<number> {
    return this.http.get<number>(`${this.getBaseUrl()}/releases/count`).pipe(catchError(this.handleError));
  }

  getDefaultRelease(): Observable<Release> {
    return this.http.get<Release>(`${this.getBaseUrl()}/releases/default`).pipe(catchError(this.handleError));
  }

  getUpcomingRelease(): Observable<Release> {
    return this.http.get<Release>(`${this.getBaseUrl()}/releases/upcomingRelease`).pipe(catchError(this.handleError));
  }

  getReleases(offset: number, limit: number): Observable<Release[]> {
    return this.http
      .get<Release[]>(`${this.getBaseUrl()}/releases?start=${offset}&limit=${limit}`)
      .pipe(catchError(this.handleError));
  }

  getAllReleases(): Observable<Release[]> {
    return this.http.get<Release[]>(`${this.getBaseUrl()}/releases`).pipe(catchError(this.handleError));
  }

  getReleaseResources(id: number): Observable<Map<string, ResourceEntity[]>> {
    return this.http.get(`${this.getBaseUrl()}/releases/${id}/resources`).pipe(
      map((jsonObject) => {
        const resourceMap = new Map<string, ResourceEntity[]>();
        for (const value in jsonObject) {
          resourceMap.set(value, jsonObject[value]);
        }
        return resourceMap;
      }),
      catchError(this.handleError),
    );
  }

  getNotDefinedReleasesForResource(resourceId: number): Observable<Release[]> {
    return this.http
      .get<Release[]>(`${this.getBaseUrl()}/releases/notDefined/${resourceId}`)
      .pipe(catchError(this.handleError));
  }

  save(release: Release) {
    if (release.id) {
      return this.update(release);
    } else {
      return this.create(release);
    }
  }

  private create(release: Release) {
    return this.http
      .post<Release>(`${this.getBaseUrl()}/releases`, release, {
        headers: this.getHeaders(),
      })
      .pipe(catchError(this.handleError));
  }

  private update(release: Release) {
    return this.http
      .put<Release>(`${this.getBaseUrl()}/releases/${release.id}`, release, {
        headers: this.getHeaders(),
      })
      .pipe(catchError(this.handleError));
  }

  delete(id: number) {
    return this.http
      .delete<number>(`${this.getBaseUrl()}/releases/${id}`, {
        headers: this.getHeaders(),
      })
      .pipe(catchError(this.handleError));
  }
}
