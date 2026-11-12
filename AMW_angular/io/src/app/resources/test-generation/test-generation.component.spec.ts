import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, convertToParamMap } from '@angular/router';
import { signal } from '@angular/core';
import { Title } from '@angular/platform-browser';
import { BehaviorSubject } from 'rxjs';
import { Resource } from '../models/resource';
import { ResourceService } from '../services/resource.service';
import { TestGenerationService } from '../services/test-generation.service';
import { EnvironmentService } from '../../deployment/environment.service';
import { TestGenerationComponent } from './test-generation.component';

describe('TestGenerationComponent', () => {
  let fixture: ComponentFixture<TestGenerationComponent>;
  let resourceSignal: ReturnType<typeof signal<Resource | null>>;
  const queryParamMap = new BehaviorSubject(convertToParamMap({ id: '42', ctx: '1' }));

  beforeEach(async () => {
    resourceSignal = signal<Resource | null>(null);
    await TestBed.configureTestingModule({
      imports: [TestGenerationComponent],
      providers: [
        { provide: ActivatedRoute, useValue: { queryParamMap } },
        {
          provide: ResourceService,
          useValue: {
            resource: resourceSignal,
            releasesForResourceGroup: signal([]),
            setIdForResource: vi.fn(),
          },
        },
        { provide: TestGenerationService, useValue: { generateTest: vi.fn() } },
        {
          provide: EnvironmentService,
          useValue: { environmentTree: signal([]), findEnvironmentById: vi.fn(() => undefined) },
        },
      ],
    });
    TestBed.overrideComponent(TestGenerationComponent, { set: { template: '' } });
    await TestBed.compileComponents();

    fixture = TestBed.createComponent(TestGenerationComponent);
    fixture.detectChanges();
  });

  it('should include the resource name in the page title', () => {
    expect(TestBed.inject(Title).getTitle()).toBe('Test Generation - Liima');

    resourceSignal.set({ id: 42, name: 'Orders' } as Resource);
    fixture.detectChanges();

    expect(TestBed.inject(Title).getTitle()).toBe('Test Generation for Orders - Liima');

    queryParamMap.next(convertToParamMap({ id: '43', ctx: '1' }));
    fixture.detectChanges();
    expect(TestBed.inject(Title).getTitle()).toBe('Test Generation for Orders - Liima');

    resourceSignal.set({ id: 43, name: 'Invoices' } as Resource);
    fixture.detectChanges();
    expect(TestBed.inject(Title).getTitle()).toBe('Test Generation for Invoices - Liima');
  });
});
