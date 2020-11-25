import { TestBed } from '@angular/core/testing';

import { DeploymentLogStoreService } from './deployment-log-store.service';

describe('DeploymentLogStoreService', () => {
  let service: DeploymentLogStoreService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(DeploymentLogStoreService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
