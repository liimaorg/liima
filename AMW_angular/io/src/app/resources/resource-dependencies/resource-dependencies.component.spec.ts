import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ResourceDependenciesComponent } from './resource-dependencies.component';
import { ResourceDependenciesService } from '../services/resource-dependencies.service';
import { ActivatedRoute, convertToParamMap } from '@angular/router';
import { BehaviorSubject, Subject } from 'rxjs';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { Title } from '@angular/platform-browser';
import { ResourceDependencies } from '../models/resource-dependency';

describe('ResourceDependenciesComponent', () => {
  let component: ResourceDependenciesComponent;
  let fixture: ComponentFixture<ResourceDependenciesComponent>;
  const queryParamMap = new BehaviorSubject(convertToParamMap({ id: '1', ctx: '1' }));
  const dependenciesResult = new Subject<ResourceDependencies>();

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ResourceDependenciesComponent],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        {
          provide: ResourceDependenciesService,
          useValue: { getResourceDependencies: vi.fn(() => dependenciesResult.asObservable()) },
        },
        {
          provide: ActivatedRoute,
          useValue: { queryParamMap },
        },
      ],
    });
    TestBed.overrideComponent(ResourceDependenciesComponent, { set: { template: '' } });
    await TestBed.compileComponents();

    fixture = TestBed.createComponent(ResourceDependenciesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should include the resource name in the page title', () => {
    expect(TestBed.inject(Title).getTitle()).toBe('Dependencies - Liima');

    dependenciesResult.next({ resourceName: 'Orders', consumedRelations: [], providedRelations: [] });
    fixture.detectChanges();

    expect(TestBed.inject(Title).getTitle()).toBe('Deps for Orders - Liima');
  });
});
