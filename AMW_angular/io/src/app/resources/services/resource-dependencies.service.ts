import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ResourceDependencies } from '../models/resource-dependency';

@Injectable({
  providedIn: 'root',
})
export class ResourceDependenciesService {
  private http = inject(HttpClient);
  private apiUrl = '/AMW_rest/resources';

  getResourceDependencies(resourceId: number): Observable<ResourceDependencies> {
    return this.http.get<ResourceDependencies>(`${this.apiUrl}/${resourceId}/dependencies`);
  }
}
