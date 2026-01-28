import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { DeploymentFilterComponent } from './deployment-filter.component';
import { DeploymentService } from '../../deployment/deployment.service';
import { DeploymentFilter } from '../../deployment/deployment-filter';
import { of } from 'rxjs';

describe('DeploymentFilterComponent', () => {
  let component: DeploymentFilterComponent;
  let fixture: ComponentFixture<DeploymentFilterComponent>;
  let deploymentService: DeploymentService;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DeploymentFilterComponent],
      providers: [DeploymentService, provideHttpClient(withInterceptorsFromDi()), provideHttpClientTesting()],
    }).compileComponents();

    fixture = TestBed.createComponent(DeploymentFilterComponent);
    component = fixture.componentInstance;
    deploymentService = TestBed.inject(DeploymentService);
  });

  it('should create', () => {
    component.filter = {
      name: 'State',
      comp: 'eq',
      val: 'failed',
    } as DeploymentFilter;
    component.index = 0;
    component.type = 'ENUM_TYPE';
    component.compOptions = [{ name: 'eq', displayName: 'is' }];
    expect(component).toBeTruthy();
  });

  it('should load options for ENUM_TYPE filters and mark for check', () => {
    const filter: DeploymentFilter = {
      name: 'State',
      comp: 'eq',
      val: 'failed',
    } as DeploymentFilter;

    component.filter = filter;
    component.index = 0;
    component.type = 'ENUM_TYPE';
    component.compOptions = [{ name: 'eq', displayName: 'is' }];

    const mockOptions = ['success', 'failed', 'canceled'];
    vi.spyOn(deploymentService, 'getFilterOptionValues').mockImplementation(() => {
      // Before async emission, the component should pre-seed valOptions with the current value
      expect(component.valOptions()).toEqual(['failed']);
      return of(mockOptions);
    });

    component.ngOnInit();

    expect(deploymentService.getFilterOptionValues).toHaveBeenCalledWith('State');
    expect(component.valOptions()).toEqual(mockOptions);
  });

  it('should set boolean options for booleanType filters and mark for check', () => {
    const filter: DeploymentFilter = {
      name: 'Confirmed',
      comp: 'eq',
      val: 'true',
    } as DeploymentFilter;

    component.filter = filter;
    component.index = 0;
    component.type = 'booleanType';
    component.compOptions = [{ name: 'eq', displayName: 'is' }];

    component.ngOnInit();

    expect(component.valOptions()).toEqual(['true', 'false']);
  });

  it('should not load options for SpecialFilterType', () => {
    const filter: DeploymentFilter = {
      name: 'Special',
      comp: 'eq',
      val: '',
    } as DeploymentFilter;

    component.filter = filter;
    component.index = 0;
    component.type = 'SpecialFilterType';
    component.compOptions = [];

    const spy = vi.spyOn(deploymentService, 'getFilterOptionValues');

    component.ngOnInit();

    expect(spy).not.toHaveBeenCalled();
    expect(component.valOptions()).toEqual([]);
  });

  it('should not load options for DateType filters', () => {
    const filter: DeploymentFilter = {
      name: 'Date',
      comp: 'eq',
      val: '',
    } as DeploymentFilter;

    component.filter = filter;
    component.index = 0;
    component.type = 'DateType';
    component.compOptions = [];

    const spy = vi.spyOn(deploymentService, 'getFilterOptionValues');

    component.ngOnInit();

    expect(spy).not.toHaveBeenCalled();
    expect(component.valOptions()).toEqual([]);
  });

  it('should emit remove event', () => {
    const filter: DeploymentFilter = {
      name: 'State',
      comp: 'eq',
      val: 'failed',
    } as DeploymentFilter;

    component.filter = filter;
    component.index = 0;
    component.type = 'ENUM_TYPE';

    let emittedFilter: DeploymentFilter | undefined;
    component.remove.subscribe((f) => (emittedFilter = f));

    component.onRemove();

    expect(emittedFilter).toBe(filter);
  });
});
