import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { DeploymentLog } from './deployment-log';
import { DeploymentLogsService } from './deployment-logs.service';

describe('DeploymentLogsService', () => {
  let service: DeploymentLogsService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [DeploymentLogsService, provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(DeploymentLogsService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('returns expected deployment logs', () => {
    const expectedDeploymentLogs: DeploymentLog[] = [
      { id: 1, filename: 'log1.log' },
      { id: 1, filename: 'log2.log' },
    ];

    let actual: DeploymentLog[] | undefined;
    service.getLogFileMetaData(1).subscribe({
      next: (logs) => (actual = logs),
      error: expect.fail,
    });

    const req = httpMock.expectOne('/AMW_rest/resources/deployments/1/logs');
    expect(req.request.method).toBe('GET');
    req.flush(expectedDeploymentLogs);

    expect(actual).toEqual(expectedDeploymentLogs);
  });

  it('handles 404 error and maps message', () => {
    let actualError: any;
    service.getLogFileMetaData(1).subscribe({
      next: () => expect.fail('expected error'),
      error: (e) => (actualError = e),
    });

    const req = httpMock.expectOne('/AMW_rest/resources/deployments/1/logs');
    expect(req.request.method).toBe('GET');
    req.flush(
      { message: 'no deployment logs found' },
      {
        status: 404,
        statusText: 'Not Found',
      },
    );

    expect(actualError).toContain('no deployment logs found');
  });

  it('getLogFileContent calls correct url', () => {
    const deploymentLog: DeploymentLog = { id: 5, filename: 'output.log' };
    let actual: DeploymentLog | undefined;
    service.getLogFileContent(deploymentLog).subscribe({
      next: (log) => (actual = log),
      error: expect.fail,
    });
    const req = httpMock.expectOne('/AMW_rest/resources/deployments/5/logs/output.log');
    expect(req.request.method).toBe('GET');
    req.flush(deploymentLog);
    expect(actual).toEqual(deploymentLog);
  });
});
