import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { BaseService } from 'src/app/base/base.service';
import { DeploymentLog } from './deployment-log';

@Injectable({
  providedIn: 'root',
})
export class DeploymentLogsService extends BaseService {
  constructor(private http: HttpClient) {
    super();
  }

  get(deploymentId: number): Observable<DeploymentLog[]> {
    return this.http
      .get<DeploymentLog[]>(
        `${this.getBaseUrl()}/deployments/${deploymentId}/logs`,
        {
          headers: this.getHeaders(),
        }
      )
      .pipe(catchError(this.handleError));
  }
}
