import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { AuditviewTableComponent } from './auditview-table.component';

describe('AuditviewTableComponent', () => {
  let component: AuditviewTableComponent;
  let fixture: ComponentFixture<AuditviewTableComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ AuditviewTableComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AuditviewTableComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
