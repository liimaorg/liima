import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { BaseService } from '../../base/base.service';
import { EnvironmentGenerationResult } from '../models/test-generation-result';

@Injectable({ providedIn: 'root' })
export class TestGenerationService extends BaseService {
  private http = inject(HttpClient);

  generateTest(
    resourceGroupName: string,
    releaseName: string,
    environmentName: string,
  ): Observable<EnvironmentGenerationResult> {
    return this.http
      .get<EnvironmentGenerationResult>(
        `${this.getBaseUrl()}/analyze/${encodeURIComponent(resourceGroupName)}/${encodeURIComponent(releaseName)}/${encodeURIComponent(environmentName)}`,
        { headers: this.getHeaders() },
      )
      .pipe(
        catchError((error: HttpErrorResponse) => {
          if (error.status === 422 && error.error) {
            return [error.error as EnvironmentGenerationResult];
          }
          return this.handleError(error);
        }),
      );
  }
}
