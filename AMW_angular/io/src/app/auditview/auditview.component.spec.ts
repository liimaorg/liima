import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';
import { Title } from '@angular/platform-browser';
import { Subject, of } from 'rxjs';
import { AuditviewComponent } from './auditview.component';
import { AuditviewService } from './auditview.service';
import { ResourceService } from '../resources/services/resource.service';

describe('AuditviewComponent', () => {
  let fixture: ComponentFixture<AuditviewComponent>;
  const resourceName = new Subject<{ name: string }>();

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AuditviewComponent],
      providers: [
        { provide: ActivatedRoute, useValue: { queryParams: of({ resourceId: '42' }) } },
        { provide: ResourceService, useValue: { getResourceName: () => resourceName.asObservable() } },
      ],
    });
    TestBed.overrideComponent(AuditviewComponent, {
      set: {
        template: '',
        providers: [{ provide: AuditviewService, useValue: { getAuditLogForResource: () => of([]) } }],
      },
    });
    await TestBed.compileComponents();

    fixture = TestBed.createComponent(AuditviewComponent);
    fixture.detectChanges();
  });

  it('should include the resource name in the page title', () => {
    expect(TestBed.inject(Title).getTitle()).toBe('Audit View - Liima');

    resourceName.next({ name: 'Orders' });
    fixture.detectChanges();

    expect(TestBed.inject(Title).getTitle()).toBe('Audit View for Orders - Liima');
  });
});
