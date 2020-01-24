import { TestBed } from '@angular/core/testing';

import { AuditviewTableService } from './auditview-table.service';

describe('AuditviewTableService', () => {
  beforeEach(() => TestBed.configureTestingModule({}));

  fit('should be created', () => {
    const service: AuditviewTableService = TestBed.get(AuditviewTableService);
    expect(service).toBeTruthy();
  });
});
