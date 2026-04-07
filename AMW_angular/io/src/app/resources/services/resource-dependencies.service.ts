import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ResourceDependencies } from '../models/resource-dependency';
import { BaseService } from '../../base/base.service';

@Injectable({
  providedIn: 'root',
})
export class ResourceDependenciesService extends BaseService {
  private http = inject(HttpClient);
  private path = `${this.getBaseUrl()}/resources`;

  getResourceDependencies(resourceId: number): Observable<ResourceDependencies> {
    return this.http.get<ResourceDependencies>(`${this.path}/${resourceId}/resource-dependencies`);
  }
}
