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
import { Deployment } from './deployment';
import { DeploymentDetail } from './deployment-detail';
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
  let filter: string = JSON.stringify([{name: 'Application', val: 'test'}, {name: 'Confirmed on', comp: 'lt', val: '12.12.2012 12:12'}]);
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
          queryParams: Observable.of({filters: filter})
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
      expect(deploymentsComponent.paramFilters[1].comp).toEqual('lt');
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

describe('DeploymentsComponent (with query params including autoplay)', () => {
  let filter: string = JSON.stringify([{name: 'Application', val: 'test'}, {name: 'Confirmed on', val: '12.12.2012 12:12'}]);
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
          queryParams: Observable.of({filters: filter, autoload: 'true'})
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

  it('should extract filters and autoload from param on ngOnInit',
    inject([DeploymentsComponent, DeploymentService], (deploymentsComponent: DeploymentsComponent, deploymentService: DeploymentService) => {
      // given
      let deploymentFilters: DeploymentFilterType[] = [ { name: 'Application', type: 'StringType' }, { name: 'Confirmed on', type: 'DateType' } ];
      spyOn(deploymentService, 'getAllDeploymentFilterTypes').and.returnValue(Observable.of(deploymentFilters));
      spyOn(deploymentService, 'getAllComparatorFilterOptions').and.returnValue(Observable.of([]));
      spyOn(deploymentService, 'canRequestDeployments').and.returnValue(Observable.of(true));

      // when
      deploymentsComponent.ngOnInit();

      // then
      expect(deploymentsComponent.autoload).toBeTruthy();
      expect(deploymentsComponent.paramFilters.length).toEqual(2);
      expect(deploymentService.canRequestDeployments).toHaveBeenCalled();
  }));

  it('should apply filters ngOnInit if autoload is true',
    inject([DeploymentsComponent, DeploymentService], (deploymentsComponent: DeploymentsComponent, deploymentService: DeploymentService) => {
      // given
      let deploymentFilters: DeploymentFilterType[] = [ { name: 'Application', type: 'StringType' }, { name: 'Confirmed on', type: 'DateType' } ];
      spyOn(deploymentService, 'getAllDeploymentFilterTypes').and.returnValue(Observable.of(deploymentFilters));
      spyOn(deploymentService, 'getAllComparatorFilterOptions').and.returnValue(Observable.of([]));
      spyOn(deploymentService, 'canRequestDeployments').and.returnValue(Observable.of(true));
      spyOn(deploymentService, 'getFilteredDeployments').and.returnValue(Observable.of([]));

      // when
      deploymentsComponent.ngOnInit();

      // then
      expect(deploymentsComponent.autoload).toBeTruthy();
      expect(deploymentService.getFilteredDeployments).toHaveBeenCalled();
  }));

});

describe('DeploymentsComponent (with illegal query params)', () => {
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
          queryParams: Observable.of({filters: 'faulty'})
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

  it('should display error message on faulty filters params on ngOnInit',
    inject([DeploymentsComponent, DeploymentService], (deploymentsComponent: DeploymentsComponent, deploymentService: DeploymentService) => {
      // given
      let deploymentFilters: DeploymentFilterType[] = [ { name: 'Application', type: 'StringType' }, { name: 'Confirmed on', type: 'DateType' } ];
      spyOn(deploymentService, 'getAllDeploymentFilterTypes').and.returnValue(Observable.of(deploymentFilters));
      spyOn(deploymentService, 'getAllComparatorFilterOptions').and.returnValue(Observable.of([]));

      // when
      deploymentsComponent.ngOnInit();

      // then
      expect(deploymentsComponent.autoload).toBeFalsy();
      expect(deploymentsComponent.errorMessage).toEqual('Error parsing filter');
      expect(deploymentsComponent.paramFilters.length).toEqual(0);
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
      expect(deploymentsComponent.autoload).toBeFalsy();
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

  it('should sort out filters without a value on apply filters',
    inject([DeploymentsComponent, DeploymentService], (deploymentsComponent: DeploymentsComponent, deploymentService: DeploymentService) => {
      // given
      let expectedFilters: DeploymentFilter[]  = [ <DeploymentFilter> { name: 'Confirmed', comp: 'eq', val: 'false' },
        <DeploymentFilter> { name: 'Application', comp: 'eq', val: 'TestApp' },
        <DeploymentFilter> { name: 'Latest deployment', comp: 'eq', val: '' } ];
      deploymentsComponent.filters = [ <DeploymentFilter> { name: 'Confirmed', comp: 'eq', val: 'false', type: 'booleanType' },
        <DeploymentFilter> { name: 'Application', comp: 'eq', val: 'TestApp', type: 'StringType' },
        <DeploymentFilter> { name: 'Application Server', comp: 'eq', val: '', type: 'StringType' },
        <DeploymentFilter> { name: 'Latest deployment', comp: 'eq', val: '', type: 'SpecialFilterType' } ];
      let deploymentFilters: DeploymentFilterType[] = [ { name: 'Application', type: 'StringType' },
        { name: 'Application Server', type: 'StringType' }, { name: 'Confirmed on', type: 'DateType' },
        { name: 'Latest deployment', type: 'SpecialFilterType' } ];
      let comparatorOptions: ComparatorFilterOption[] = [ { name: 'lt', displayName: '<'}, { name: 'eq', displayName: 'is' },
        { name: 'neq', displayName: 'is not' }];
      spyOn(deploymentService, 'getAllDeploymentFilterTypes').and.returnValue(Observable.of(deploymentFilters));
      spyOn(deploymentService, 'getAllComparatorFilterOptions').and.returnValue(Observable.of(comparatorOptions));
      spyOn(deploymentService, 'getFilteredDeployments').and.returnValue(Observable.of([]));

      // when
      deploymentsComponent.applyFilter();

      // then
      expect(deploymentsComponent.filters.length).toEqual(3);
      expect(deploymentService.getFilteredDeployments).toHaveBeenCalledWith(JSON.stringify(expectedFilters));
  }));

  it('should check permission on showEdit',
    inject([DeploymentsComponent, DeploymentService, ResourceService],
      (deploymentsComponent: DeploymentsComponent, deploymentService: DeploymentService, resourceService: ResourceService) => {
      // given
      deploymentsComponent.deployments = [ <Deployment> { id: 1, appServerId: 12, selected: false }, <Deployment> { id: 21, appServerId: 22, selected: true } ];
      spyOn(resourceService, 'canCreateShakedownTest').and.returnValue(Observable.of(true));

      // when
      deploymentsComponent.showEdit();

      // then
      expect(resourceService.canCreateShakedownTest).toHaveBeenCalledWith(22);
  }));

  it('should confirm a deployment and reload it',
    inject([DeploymentsComponent, DeploymentService],
      (deploymentsComponent: DeploymentsComponent, deploymentService: DeploymentService) => {
      // given
      let detail: DeploymentDetail = <DeploymentDetail> { deploymentId: 1 };
      let deployment: Deployment = <Deployment> { id: 1 };
      spyOn(deploymentService, 'confirmDeployment').and.returnValue(Observable.of());
      spyOn(deploymentService, 'getWithActions').and.returnValue(Observable.of(deployment));

      // when
      deploymentsComponent.confirmDeployment(detail);

      // then
      expect(deploymentService.confirmDeployment).toHaveBeenCalledWith(detail);
      expect(deploymentService.getWithActions).toHaveBeenCalledWith(detail.deploymentId);
  }));

  it('should reject a deployment and reload it',
    inject([DeploymentsComponent, DeploymentService],
      (deploymentsComponent: DeploymentsComponent, deploymentService: DeploymentService) => {
      // given
      let deployment: Deployment = <Deployment> { id: 2 };
      spyOn(deploymentService, 'rejectDeployment').and.returnValue(Observable.of());
      spyOn(deploymentService, 'getWithActions').and.returnValue(Observable.of(deployment));

      // when
      deploymentsComponent.rejectDeployment(deployment);

      // then
      expect(deploymentService.rejectDeployment).toHaveBeenCalledWith(deployment.id);
      expect(deploymentService.getWithActions).toHaveBeenCalledWith(deployment.id);
  }));

  it('should cancel a deployment and reload it',
    inject([DeploymentsComponent, DeploymentService],
      (deploymentsComponent: DeploymentsComponent, deploymentService: DeploymentService) => {
      // given
      let deployment: Deployment = <Deployment> { id: 3 };
      spyOn(deploymentService, 'cancelDeployment').and.returnValue(Observable.of());
      spyOn(deploymentService, 'getWithActions').and.returnValue(Observable.of(deployment));

      // when
      deploymentsComponent.cancelDeployment(deployment);

      // then
      expect(deploymentService.cancelDeployment).toHaveBeenCalledWith(deployment.id);
      expect(deploymentService.getWithActions).toHaveBeenCalledWith(deployment.id);
  }));

});

