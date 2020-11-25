import { TestBed } from '@angular/core/testing';

import { DeploymentsStoreService } from './deployments-store.service';

describe('DeploymentsStoreService', () => {
  let service: DeploymentsStoreService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(DeploymentsStoreService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
