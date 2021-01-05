import { HttpClientTestingModule } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { DeploymentLogService } from './deployment-log.service';

describe('DeploymentLogService', () => {
  let service: DeploymentLogService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
    });
    service = TestBed.inject(DeploymentLogService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
