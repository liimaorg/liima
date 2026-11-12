import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { ResourceTypeEditComponent } from './resource-type-edit.component';
import { ActivatedRoute, convertToParamMap } from '@angular/router';
import { BehaviorSubject } from 'rxjs';
import { signal } from '@angular/core';
import { Title } from '@angular/platform-browser';
import { ResourceType } from '../models/resource-type';
import { ResourceTypesService } from '../services/resource-types.service';

describe('ResourceEditPageComponent', () => {
  let component: ResourceTypeEditComponent;
  let fixture: ComponentFixture<ResourceTypeEditComponent>;
  let resourceTypeSignal: ReturnType<typeof signal<ResourceType | null>>;
  const queryParamMap = new BehaviorSubject(convertToParamMap({ id: '42', ctx: '1' }));

  beforeEach(async () => {
    resourceTypeSignal = signal<ResourceType | null>(null);
    await TestBed.configureTestingModule({
      imports: [ResourceTypeEditComponent],
      providers: [
        provideHttpClient(withInterceptorsFromDi()),
        provideHttpClientTesting(),
        { provide: ActivatedRoute, useValue: { queryParamMap } },
        {
          provide: ResourceTypesService,
          useValue: { resourceType: resourceTypeSignal, setIdForResourceType: vi.fn() },
        },
      ],
    });
    TestBed.overrideComponent(ResourceTypeEditComponent, { set: { template: '' } });
    await TestBed.compileComponents();

    fixture = TestBed.createComponent(ResourceTypeEditComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should include the resource type name in the page title', () => {
    expect(TestBed.inject(Title).getTitle()).toBe('Edit Resource Type - Liima');

    resourceTypeSignal.set({
      id: 42,
      name: 'Database',
      hasChildren: false,
      hasParent: false,
      children: [],
      isApplication: false,
      isDefaultResourceType: false,
    });
    fixture.detectChanges();

    expect(TestBed.inject(Title).getTitle()).toBe('Edit Database - Liima');

    queryParamMap.next(convertToParamMap({ id: '43', ctx: '1' }));
    fixture.detectChanges();
    expect(TestBed.inject(Title).getTitle()).toBe('Edit Database - Liima');

    resourceTypeSignal.set({
      id: 43,
      name: 'Queue',
      hasChildren: false,
      hasParent: false,
      children: [],
      isApplication: false,
      isDefaultResourceType: false,
    });
    fixture.detectChanges();
    expect(TestBed.inject(Title).getTitle()).toBe('Edit Queue - Liima');
  });
});
