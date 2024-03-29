import { CommonModule, DatePipe } from '@angular/common';
import { TestBed } from '@angular/core/testing';
import { AuditviewTableService } from './auditview-table.service';

describe('AuditviewTableService', () => {
  beforeEach(() =>
    TestBed.configureTestingModule({
      imports: [CommonModule],
      providers: [DatePipe],
    }),
  );

  it('should be created', () => {
    const service: AuditviewTableService = TestBed.inject(AuditviewTableService);
    expect(service).toBeTruthy();
  });
});
