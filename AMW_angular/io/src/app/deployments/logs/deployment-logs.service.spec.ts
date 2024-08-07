import { HttpErrorResponse } from '@angular/common/http';
import { defer } from 'rxjs';
import { DeploymentLog } from './deployment-log';
import { DeploymentLogsService } from './deployment-logs.service';

/** Create async observable that emits-once and completes
 *  after a JS engine turn */
export function asyncData<T>(data: T) {
  return defer(() => Promise.resolve(data));
}

/**
 * Create async observable error that errors
 * after a JS engine turn
 */
export function asyncError<T>(errorObject: any) {
  return defer(() => Promise.reject(errorObject));
}

describe('DeploymentLogService', () => {
  let service: DeploymentLogsService;
  let httpClientSpy: { get: jasmine.Spy };

  beforeEach(() => {
    httpClientSpy = jasmine.createSpyObj('HttpClient', ['get']);
    service = new DeploymentLogsService(httpClientSpy as any);
  });

  it('should return expected deployment logs', () => {
    const expectedDeploymentLogs: DeploymentLog[] = [
      { id: 1, filename: 'log1.log' },
      { id: 1, filename: 'log2.log' },
    ];

    httpClientSpy.get.and.returnValue(asyncData(expectedDeploymentLogs));

    service.getLogFileMetaData(1).subscribe({
      next: (deploymentLogs) =>
        expect(deploymentLogs).withContext('expected deploymentLogs').toEqual(expectedDeploymentLogs),
      error: fail,
    });

    expect(httpClientSpy.get.calls.count()).withContext('called once').toBe(1);
  });

  it('should handle 404', () => {
    const errorResponse = new HttpErrorResponse({
      error: { message: 'no deployment logs found' },
      status: 404,
      statusText: 'Not Found',
    });

    httpClientSpy.get.and.returnValue(asyncError(errorResponse));

    service.getLogFileMetaData(1).subscribe({
      next: (_) => fail('expected an error, not a list of deploymentLogs'),
      error: (error) => expect(error).toContain('no deployment logs found'),
    });
  });
});
