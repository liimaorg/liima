import {
  async,
  ComponentFixture,
  ComponentFixtureAutoDetect,
  TestBed
} from '@angular/core/testing';
import { FormsModule } from '@angular/forms';
import { NgbModule } from '@ng-bootstrap/ng-bootstrap';
import { NewlineFilterPipe } from 'src/app/customfilter/newlineFilterPipe';
import { AuditviewTableComponent } from './auditview-table.component';
import { DatePipe } from '@angular/common';

describe('AuditviewTableComponent', () => {
  let component: AuditviewTableComponent;
  let fixture: ComponentFixture<AuditviewTableComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [FormsModule, NgbModule],
      declarations: [AuditviewTableComponent, NewlineFilterPipe],
      providers: [DatePipe]
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AuditviewTableComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });
});
