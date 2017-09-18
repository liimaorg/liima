import { Component } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { inject, TestBed } from '@angular/core/testing';
import { BaseRequestOptions, ConnectionBackend, Http } from '@angular/http';
import { MockBackend } from '@angular/http/testing';
import { CommonModule } from '@angular/common';
import { RouterTestingModule } from '@angular/router/testing';
import { Observable } from 'rxjs';
import { AppState } from '../app.service';
import { ComparatorFilterOption } from './comparator-filter-option';
import { DeploymentsComponent } from './deployments.component';
import { DeploymentFilter } from './deployment-filter';
import { DeploymentFilterType } from './deployment-filter-type';
import { DeploymentService } from './deployment.service';
import { ResourceService } from '../resource/resource.service';

@Component({
  template: ''
})
class DummyComponent {
}

describe('DeploymentsComponent (with query params)', () => {
  let filter: string = JSON.stringify([{name: 'Application', val: 'test'}, {name: 'Confirmed on', val: '1000110001'}]);
  // provide our implementations or mocks to the dependency injector
  beforeEach(() => TestBed.configureTestingModule({
    imports: [
      CommonModule,
      RouterTestingModule.withRoutes([
        {path: 'deployments', component: DummyComponent}
      ])
    ],
    providers: [
      BaseRequestOptions, {
        provide: ActivatedRoute,
        useValue: {
          queryParams: Observable.of({filters: [ filter ]})
        },
      },
      MockBackend,
      {
        provide: Http,
        useFactory: function (backend: ConnectionBackend, defaultOptions: BaseRequestOptions) {
          return new Http(backend, defaultOptions);
        },
        deps: [MockBackend, BaseRequestOptions]
      },
      DeploymentService,
      ResourceService,
      DeploymentsComponent,
      AppState
    ],
    declarations: [DummyComponent],
  }));

  it('should extract filters from param on ngOnInit',
    inject([DeploymentsComponent, DeploymentService], (deploymentsComponent: DeploymentsComponent, deploymentService: DeploymentService) => {
      // given
      let deploymentFilters: DeploymentFilterType[] = [ { name: 'Application', type: 'StringType' }, { name: 'Confirmed on', type: 'DateType' } ];
      spyOn(deploymentService, 'getAllDeploymentFilterTypes').and.returnValue(Observable.of(deploymentFilters));
      spyOn(deploymentService, 'getAllComparatorFilterOptions').and.returnValue(Observable.of([]));
      spyOn(deploymentService, 'canRequestDeployments').and.returnValue(Observable.of(true));

      // when
      deploymentsComponent.ngOnInit();

      // then
      expect(deploymentsComponent.paramFilters.length).toEqual(2);
      expect(deploymentService.canRequestDeployments).toHaveBeenCalled();
  }));

  it('should enhance filters with the right comparator and comparator options on ngOnInit',
    inject([DeploymentsComponent, DeploymentService], (deploymentsComponent: DeploymentsComponent, deploymentService: DeploymentService) => {
      // given
      let deploymentFilters: DeploymentFilterType[] = [ { name: 'Application', type: 'StringType' }, { name: 'Confirmed on', type: 'DateType' } ];
      let comparatorOptions: ComparatorFilterOption[] = [ { name: 'lt', displayName: '<'}, { name: 'eq', displayName: 'is' }, { name: 'neq', displayName: 'is not' }];
      spyOn(deploymentService, 'getAllDeploymentFilterTypes').and.returnValue(Observable.of(deploymentFilters));
      spyOn(deploymentService, 'getAllComparatorFilterOptions').and.returnValue(Observable.of(comparatorOptions));
      spyOn(deploymentService, 'canRequestDeployments').and.returnValue(Observable.of(true));

      // when
      deploymentsComponent.ngOnInit();

      // then
      expect(deploymentsComponent.paramFilters[0].compOptions.length).toEqual(1);
      expect(deploymentsComponent.paramFilters[1].compOptions.length).toEqual(3);
      expect(deploymentsComponent.paramFilters[0].comp).toEqual('eq');
      expect(deploymentsComponent.paramFilters[1].comp).toEqual('eq');
  }));

  it('should enhance filters with the right option values on ngOnInit',
    inject([DeploymentsComponent, DeploymentService], (deploymentsComponent: DeploymentsComponent, deploymentService: DeploymentService) => {
      // given
      let deploymentFilters: DeploymentFilterType[] = [ { name: 'Application', type: 'StringType' }, { name: 'Confirmed on', type: 'DateType' } ];
      spyOn(deploymentService, 'getAllDeploymentFilterTypes').and.returnValue(Observable.of(deploymentFilters));
      spyOn(deploymentService, 'getAllComparatorFilterOptions').and.returnValue(Observable.of([]));
      spyOn(deploymentService, 'getFilterOptionValues').and.callFake(function(param: string) {
        let optionValues: { [key: string]: string[] } = { 'Application': ['app1', 'app2'],  'Confirmed on': [] };
        return Observable.of(optionValues[param]);
      });
      spyOn(deploymentService, 'canRequestDeployments').and.returnValue(Observable.of(true));

      // when
      deploymentsComponent.ngOnInit();

      // then
      expect(deploymentsComponent.paramFilters[0].valOptions.length).toEqual(2);
      expect(deploymentsComponent.paramFilters[1].valOptions.length).toEqual(0);
  }));

});

describe('DeploymentsComponent (without query params)', () => {
  // provide our implementations or mocks to the dependency injector
  beforeEach(() => TestBed.configureTestingModule({
    imports: [
      CommonModule,
      RouterTestingModule.withRoutes([
        {path: 'deployments', component: DummyComponent}
      ])
    ],
    providers: [
      BaseRequestOptions, {
        provide: ActivatedRoute,
        useValue: {
          queryParams: Observable.of([])
        },
      },
      MockBackend,
      {
        provide: Http,
        useFactory: function (backend: ConnectionBackend, defaultOptions: BaseRequestOptions) {
          return new Http(backend, defaultOptions);
        },
        deps: [MockBackend, BaseRequestOptions]
      },
      DeploymentService,
      ResourceService,
      DeploymentsComponent,
      AppState
    ],
    declarations: [DummyComponent],
  }));

  it('should check permission on ngOnInit',
    inject([DeploymentsComponent, DeploymentService], (deploymentsComponent: DeploymentsComponent, deploymentService: DeploymentService) => {
      // given
      spyOn(deploymentService, 'canRequestDeployments').and.returnValue(Observable.of(true));

      // when
      deploymentsComponent.ngOnInit();

      // then
      expect(deploymentsComponent.paramFilters.length).toEqual(0);
      expect(deploymentService.canRequestDeployments).toHaveBeenCalled();
  }));

  it('should add a filter',
    inject([DeploymentsComponent, DeploymentService], (deploymentsComponent: DeploymentsComponent, deploymentService: DeploymentService) => {
      // given
      deploymentsComponent.filters = [ <DeploymentFilter> { name: 'Confirmed', comp: 'eq', val: 'true', type: 'booleanType' } ];
      deploymentsComponent.selectedFilterType = { name: 'Application', type: 'StringType' };
      let deploymentFilters: DeploymentFilterType[] = [ { name: 'Application', type: 'StringType' }, { name: 'Confirmed on', type: 'DateType' } ];
      let comparatorOptions: ComparatorFilterOption[] = [ { name: 'lt', displayName: '<'}, { name: 'eq', displayName: 'is' }, { name: 'neq', displayName: 'is not' }];
      spyOn(deploymentService, 'getAllDeploymentFilterTypes').and.returnValue(Observable.of(deploymentFilters));
      spyOn(deploymentService, 'getAllComparatorFilterOptions').and.returnValue(Observable.of(comparatorOptions));
      expect(deploymentsComponent.filters.length).toEqual(1);

      // when
      deploymentsComponent.addFilter();

      // then
      expect(deploymentsComponent.filters.length).toEqual(2);
  }));

  it('should apply filters',
    inject([DeploymentsComponent, DeploymentService], (deploymentsComponent: DeploymentsComponent, deploymentService: DeploymentService) => {
      // given
      let expectedFilters: DeploymentFilter[]  = [ <DeploymentFilter> { name: 'Confirmed', comp: 'eq', val: 'true' },
        <DeploymentFilter> { name: 'Application', comp: 'eq', val: 'TestApp' } ];
      deploymentsComponent.filters = [ <DeploymentFilter> { name: 'Confirmed', comp: 'eq', val: 'true', type: 'booleanType' },
        <DeploymentFilter> { name: 'Application', comp: 'eq', val: 'TestApp', type: 'StringType' } ];
      let deploymentFilters: DeploymentFilterType[] = [ { name: 'Application', type: 'StringType' }, { name: 'Confirmed on', type: 'DateType' } ];
      let comparatorOptions: ComparatorFilterOption[] = [ { name: 'lt', displayName: '<'}, { name: 'eq', displayName: 'is' }, { name: 'neq', displayName: 'is not' }];
      spyOn(deploymentService, 'getAllDeploymentFilterTypes').and.returnValue(Observable.of(deploymentFilters));
      spyOn(deploymentService, 'getAllComparatorFilterOptions').and.returnValue(Observable.of(comparatorOptions));
      spyOn(deploymentService, 'getFilteredDeployments').and.returnValue(Observable.of([]));

      // when
      deploymentsComponent.applyFilter();

      // then
      expect(deploymentService.getFilteredDeployments).toHaveBeenCalledWith(JSON.stringify(expectedFilters));
  }));

});
