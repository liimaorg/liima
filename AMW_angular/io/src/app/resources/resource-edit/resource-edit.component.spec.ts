import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { ResourceEditComponent } from './resource-edit.component';
import { ActivatedRoute, convertToParamMap } from '@angular/router';
import { BehaviorSubject } from 'rxjs';
import { signal } from '@angular/core';
import { Title } from '@angular/platform-browser';
import { ResourceService } from '../services/resource.service';
import { Resource } from '../models/resource';

describe('ResourceEditComponent', () => {
  let component: ResourceEditComponent;
  let fixture: ComponentFixture<ResourceEditComponent>;
  let resourceSignal: ReturnType<typeof signal<Resource | null>>;
  const queryParamMap = new BehaviorSubject(convertToParamMap({ id: '42', ctx: '1' }));

  beforeEach(async () => {
    resourceSignal = signal<Resource | null>(null);
    await TestBed.configureTestingModule({
      imports: [ResourceEditComponent],
      providers: [
        provideHttpClient(withInterceptorsFromDi()),
        provideHttpClientTesting(),
        { provide: ActivatedRoute, useValue: { queryParamMap, snapshot: { queryParamMap: queryParamMap.value } } },
        {
          provide: ResourceService,
          useValue: {
            resource: resourceSignal,
            releasesForResourceGroup: signal([]),
            isLoadingResource: signal(false),
            setIdForResource: vi.fn(),
          },
        },
      ],
    });
    TestBed.overrideComponent(ResourceEditComponent, { set: { template: '' } });
    await TestBed.compileComponents();

    fixture = TestBed.createComponent(ResourceEditComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  afterEach(() => {
    queryParamMap.next(convertToParamMap({ id: '42', ctx: '1' }));
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should include the resource name in the page title', () => {
    expect(TestBed.inject(Title).getTitle()).toBe('Edit Resource - Liima');

    resourceSignal.set({ id: 42, name: 'Orders' } as Resource);
    fixture.detectChanges();
    expect(TestBed.inject(Title).getTitle()).toBe('Edit Orders - Liima');

    queryParamMap.next(convertToParamMap({ id: '43', ctx: '1' }));
    fixture.detectChanges();
    expect(TestBed.inject(Title).getTitle()).toBe('Edit Orders - Liima');

    resourceSignal.set({ id: 43, name: 'Invoices' } as Resource);
    fixture.detectChanges();

    expect(TestBed.inject(Title).getTitle()).toBe('Edit Invoices - Liima');

    queryParamMap.next(convertToParamMap({ id: '43', ctx: '2', rel: '7' }));
    fixture.detectChanges();

    expect(TestBed.inject(Title).getTitle()).toBe('Edit Invoices - Liima');
  });
});
