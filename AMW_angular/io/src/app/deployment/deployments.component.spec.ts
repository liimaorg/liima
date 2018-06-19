import { Component } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { inject, TestBed } from '@angular/core/testing';
import { BaseRequestOptions, ConnectionBackend, Http } from '@angular/http';
import { MockBackend } from '@angular/http/testing';
import { CommonModule } from '@angular/common';
import { NgModel } from '@angular/forms';
import { RouterTestingModule } from '@angular/router/testing';
import { Observable } from 'rxjs';
import { AppState } from '../app.service';
import { ComparatorFilterOption } from './comparator-filter-option';
import { DeploymentsComponent } from './deployments.component';
import { Deployment } from './deployment';
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
  const filter: string = JSON.stringify([{name: 'Application', val: 'test'}, {name: 'Confirmed on', comp: 'lt', val: '12.12.2012 12:12'}]);
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
        useFactory(backend: ConnectionBackend, defaultOptions: BaseRequestOptions) {
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
      const deploymentFilters: DeploymentFilterType[] = [{name: 'Application', type: 'StringType'}, {name: 'Confirmed on', type: 'DateType'}];
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
      const deploymentFilters: DeploymentFilterType[] = [{name: 'Application', type: 'StringType'}, {name: 'Confirmed on', type: 'DateType'}];
      const comparatorOptions: ComparatorFilterOption[] = [{name: 'lt', displayName: '<'}, {name: 'eq', displayName: 'is'}, {name: 'neq', displayName: 'is not' }];
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
      const deploymentFilters: DeploymentFilterType[] = [{name: 'Application', type: 'StringType'}, {name: 'Confirmed on', type: 'DateType'}];
      spyOn(deploymentService, 'getAllDeploymentFilterTypes').and.returnValue(Observable.of(deploymentFilters));
      spyOn(deploymentService, 'getAllComparatorFilterOptions').and.returnValue(Observable.of([]));
      spyOn(deploymentService, 'getFilterOptionValues').and.callFake(function(param: string) {
        const optionValues: {[key: string]: string[]} = {'Application': ['app1', 'app2'],  'Confirmed on': []};
        return Observable.of(optionValues[param]);
      });
      spyOn(deploymentService, 'canRequestDeployments').and.returnValue(Observable.of(true));

      // when
      deploymentsComponent.ngOnInit();

      // then
      expect(deploymentsComponent.paramFilters[0].valOptions.length).toEqual(2);
      expect(deploymentsComponent.paramFilters[1].valOptions.length).toEqual(0);
  }));

  it('should apply filters ngOnInit ',
    inject([DeploymentsComponent, DeploymentService], (deploymentsComponent: DeploymentsComponent, deploymentService: DeploymentService) => {
      // given
      const deploymentFilters: DeploymentFilterType[] = [{name: 'Application', type: 'StringType'}, {name: 'Confirmed on', type: 'DateType'}];
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

  it('should call the right service method on exportCSV ',
    inject([DeploymentsComponent, DeploymentService], (deploymentsComponent: DeploymentsComponent, deploymentService: DeploymentService) => {
      // given
      const deploymentFilters: DeploymentFilterType[] = [{name: 'Application', type: 'StringType'}, {name: 'Confirmed on', type: 'DateType'}];
      spyOn(deploymentService, 'getFilteredDeploymentsForCsvExport').and.returnValue(Observable.of('c;s;v;'));

      // when
      deploymentsComponent.exportCSV();

      // then
      expect(deploymentService.getFilteredDeploymentsForCsvExport).toHaveBeenCalledWith(JSON.stringify(deploymentsComponent.filtersForBackend), 'd.deploymentDate', 'DESC');
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
        useFactory(backend: ConnectionBackend, defaultOptions: BaseRequestOptions) {
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
      const deploymentFilters: DeploymentFilterType[] = [{name: 'Application', type: 'StringType'}, {name: 'Confirmed on', type: 'DateType'}];
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
        useFactory(backend: ConnectionBackend, defaultOptions: BaseRequestOptions) {
          return new Http(backend, defaultOptions);
        },
        deps: [MockBackend, BaseRequestOptions]
      },
      DeploymentService,
      ResourceService,
      DeploymentsComponent,
      AppState,
      NgModel
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
      expect(deploymentService.canRequestDeployments).toHaveBeenCalled();
  }));

  it('should add a filter',
    inject([DeploymentsComponent, DeploymentService, NgModel],
      (deploymentsComponent: DeploymentsComponent, deploymentService: DeploymentService, selectModel: NgModel) => {
      // given
      deploymentsComponent.selectModel = selectModel;
      deploymentsComponent.filters = [{name: 'Confirmed', comp: 'eq', val: 'true', type: 'booleanType'} as DeploymentFilter];
      deploymentsComponent.selectedFilterType = {name: 'Application', type: 'StringType'};
      const deploymentFilters: DeploymentFilterType[] = [{name: 'Application', type: 'StringType'}, {name: 'Confirmed on', type: 'DateType'}];
      const comparatorOptions: ComparatorFilterOption[] = [{name: 'lt', displayName: '<'}, {name: 'eq', displayName: 'is'}, {name: 'neq', displayName: 'is not'}];
      spyOn(deploymentService, 'getAllDeploymentFilterTypes').and.returnValue(Observable.of(deploymentFilters));
      spyOn(deploymentService, 'getAllComparatorFilterOptions').and.returnValue(Observable.of(comparatorOptions));
      expect(deploymentsComponent.filters.length).toEqual(1);

      // when
      deploymentsComponent.addFilter();

      // then
      expect(deploymentsComponent.filters.length).toEqual(2);
  }));

  it('should reset offset and selectedFilterType on addFilter',
    inject([DeploymentsComponent, DeploymentService, NgModel],
      (deploymentsComponent: DeploymentsComponent, deploymentService: DeploymentService, selectModel: NgModel) => {
      // given
      deploymentsComponent.selectModel = selectModel;
      deploymentsComponent.offset = 10;
      deploymentsComponent.filters = [{name: 'Confirmed', comp: 'eq', val: 'true', type: 'booleanType'} as DeploymentFilter];
      deploymentsComponent.selectedFilterType = {name: 'Application', type: 'StringType'};
      const deploymentFilters: DeploymentFilterType[] = [{name: 'Application', type: 'StringType'}, {name: 'Confirmed on', type: 'DateType'}];
      const comparatorOptions: ComparatorFilterOption[] = [{name: 'lt', displayName: '<'}, {name: 'eq', displayName: 'is'}, {name: 'neq', displayName: 'is not'}];
      spyOn(deploymentService, 'getAllDeploymentFilterTypes').and.returnValue(Observable.of(deploymentFilters));
      spyOn(deploymentService, 'getAllComparatorFilterOptions').and.returnValue(Observable.of(comparatorOptions));

      // when
      deploymentsComponent.addFilter();

      // then
      expect(deploymentsComponent.offset).toEqual(0);
      expect(deploymentsComponent.selectedFilterType).toBeNull();
  }));

  it('should remove filter and reset offset on removeFilter',
    inject([DeploymentsComponent, DeploymentService], (deploymentsComponent: DeploymentsComponent, deploymentService: DeploymentService) => {
      // given
      deploymentsComponent.offset = 10;
      deploymentsComponent.filters = [{name: 'Confirmed', comp: 'eq', val: 'true', type: 'booleanType'} as DeploymentFilter];
      const deploymentFilters: DeploymentFilterType[] = [{name: 'Application', type: 'StringType'}, {name: 'Confirmed on', type: 'DateType'}];
      const comparatorOptions: ComparatorFilterOption[] = [{name: 'lt', displayName: '<'}, {name: 'eq', displayName: 'is'}, {name: 'neq', displayName: 'is not'}];
      spyOn(deploymentService, 'getAllDeploymentFilterTypes').and.returnValue(Observable.of(deploymentFilters));
      spyOn(deploymentService, 'getAllComparatorFilterOptions').and.returnValue(Observable.of(comparatorOptions));

      // when
      deploymentsComponent.removeFilter(deploymentsComponent.filters[0]);

      // then
      expect(deploymentsComponent.filters.length).toEqual(0);
      expect(deploymentsComponent.offset).toEqual(0);
  }));

  it('should reset offset on setMaxResultsPerPage',
    inject([DeploymentsComponent], (deploymentsComponent: DeploymentsComponent) => {
      // given
      deploymentsComponent.offset = 10;
      deploymentsComponent.filters = [{name: 'Confirmed', comp: 'eq', val: 'true', type: 'booleanType'} as DeploymentFilter];

      // when
      deploymentsComponent.setMaxResultsPerPage(20);

      // then
      expect(deploymentsComponent.offset).toEqual(0);
  }));

  it('should not add latest deployment job filter more than once',
    inject([DeploymentsComponent, DeploymentService, NgModel],
      (deploymentsComponent: DeploymentsComponent, deploymentService: DeploymentService, selectModel: NgModel) => {
      // given
      deploymentsComponent.selectModel = selectModel;
      deploymentsComponent.filters = [{name: 'Confirmed', comp: 'eq', val: 'true', type: 'booleanType'} as DeploymentFilter];
      deploymentsComponent.selectedFilterType = {name: 'Latest deployment job for App Server and Env', type: 'SpecialFilterType'};
      const deploymentFilters: DeploymentFilterType[] = [{name: 'Latest deployment job for App Server and Env', type: 'SpecialFilterType'},
        {name: 'Confirmed on', type: 'DateType'}];
      const comparatorOptions: ComparatorFilterOption[] = [{name: 'lt', displayName: '<'}, {name: 'eq', displayName: 'is'},
        {name: 'neq', displayName: 'is not'}];
      spyOn(deploymentService, 'getAllDeploymentFilterTypes').and.returnValue(Observable.of(deploymentFilters));
      spyOn(deploymentService, 'getAllComparatorFilterOptions').and.returnValue(Observable.of(comparatorOptions));
      expect(deploymentsComponent.filters.length).toEqual(1);

      // when
      deploymentsComponent.addFilter();

      // then
      expect(deploymentsComponent.filters.length).toEqual(2);

      // when
      deploymentsComponent.selectedFilterType = {name: 'Latest deployment job for App Server and Env', type: 'SpecialFilterType'};
      deploymentsComponent.addFilter();

      // then
      expect(deploymentsComponent.filters.length).toEqual(2);
  }));

  it('should apply filters and add them to the sessionStorage on applyFilters',
    inject([DeploymentsComponent, DeploymentService], (deploymentsComponent: DeploymentsComponent, deploymentService: DeploymentService) => {
      // given
      sessionStorage.setItem('deploymentFilters', null);
      const expectedFilters: DeploymentFilter[]  = [{name: 'Confirmed', comp: 'eq', val: 'true'} as DeploymentFilter,
        {name: 'Application', comp: 'eq', val: 'TestApp'} as DeploymentFilter];
      const deploymentFilters: DeploymentFilterType[] = [{name: 'Application', type: 'StringType'}, {name: 'Confirmed on', type: 'DateType'}];
      const comparatorOptions: ComparatorFilterOption[] = [{name: 'lt', displayName: '<'}, {name: 'eq', displayName: 'is'}, {name: 'neq', displayName: 'is not' }];
      spyOn(deploymentService, 'getAllDeploymentFilterTypes').and.returnValue(Observable.of(deploymentFilters));
      spyOn(deploymentService, 'getAllComparatorFilterOptions').and.returnValue(Observable.of(comparatorOptions));
      spyOn(deploymentService, 'getFilteredDeployments').and.returnValue(Observable.of([]));

      // when
      deploymentsComponent.ngOnInit();

      // then
      expect(deploymentService.getFilteredDeployments).toHaveBeenCalledWith('[]', 'd.deploymentDate', 'DESC', 0, 10);

      // given
      deploymentsComponent.filters = [{name: 'Confirmed', comp: 'eq', val: 'true', type: 'booleanType'} as DeploymentFilter,
        {name: 'Application', comp: 'eq', val: 'TestApp', type: 'StringType'} as DeploymentFilter];

      // when
      deploymentsComponent.applyFilters();

      // then
      expect(sessionStorage.getItem('deploymentFilters')).toEqual(JSON.stringify(expectedFilters));
      expect(deploymentService.getFilteredDeployments).toHaveBeenCalledWith(JSON.stringify(expectedFilters), 'd.deploymentDate', 'DESC', 0, 10);
  }));

  it('should sort out filters without a value on apply filters and add the remaining to the sessionStorage on applyFilters',
    inject([DeploymentsComponent, DeploymentService], (deploymentsComponent: DeploymentsComponent, deploymentService: DeploymentService) => {
      // given
      sessionStorage.setItem('deploymentFilters', null);
      const expectedFilters: DeploymentFilter[]  = [{name: 'Confirmed', comp: 'eq', val: 'false'} as DeploymentFilter,
        {name: 'Application', comp: 'eq', val: 'TestApp'} as DeploymentFilter,
        {name: 'Latest deployment', comp: 'eq', val: '' } as DeploymentFilter];
      const deploymentFilters: DeploymentFilterType[] = [{name: 'Application', type: 'StringType'},
        {name: 'Application Server', type: 'StringType'}, {name: 'Confirmed on', type: 'DateType'},
        {name: 'Latest deployment', type: 'SpecialFilterType'}];
      const comparatorOptions: ComparatorFilterOption[] = [{name: 'lt', displayName: '<'},
        {name: 'eq', displayName: 'is'}, {name: 'neq', displayName: 'is not'}];
      spyOn(deploymentService, 'getAllDeploymentFilterTypes').and.returnValue(Observable.of(deploymentFilters));
      spyOn(deploymentService, 'getAllComparatorFilterOptions').and.returnValue(Observable.of(comparatorOptions));
      spyOn(deploymentService, 'getFilteredDeployments').and.returnValue(Observable.of([]));

      // when
      deploymentsComponent.ngOnInit();

      // then
      expect(deploymentService.getFilteredDeployments).toHaveBeenCalledWith('[]', 'd.deploymentDate', 'DESC', 0, 10);

      // given
      deploymentsComponent.filters = [{name: 'Confirmed', comp: 'eq', val: 'false', type: 'booleanType'} as DeploymentFilter,
        {name: 'Application', comp: 'eq', val: 'TestApp', type: 'StringType'} as DeploymentFilter,
        {name: 'Application Server', comp: 'eq', val: '', type: 'StringType'} as DeploymentFilter,
        {name: 'Latest deployment', comp: 'eq', val: '', type: 'SpecialFilterType'} as DeploymentFilter];

      // when
      deploymentsComponent.applyFilters();

      // then
      expect(deploymentsComponent.filters.length).toEqual(3);
      expect(sessionStorage.getItem('deploymentFilters')).toEqual(JSON.stringify(expectedFilters));
      expect(deploymentService.getFilteredDeployments).toHaveBeenCalledWith(JSON.stringify(expectedFilters), 'd.deploymentDate', 'DESC', 0, 10);
  }));

  it('should clear filters and session storage',
    inject([DeploymentsComponent], (deploymentsComponent: DeploymentsComponent) => {
      // given
      sessionStorage.setItem('deploymentFilters', "{name: 'Confirmed', comp: 'eq', val: 'true'}");

      // when
      deploymentsComponent.clearFilters();

      // then
      expect(sessionStorage.getItem('deploymentFilters')).toBe('null');
  }));

  it('should invoke service with right params on sort',
    inject([DeploymentsComponent, DeploymentService], (deploymentsComponent: DeploymentsComponent, deploymentService: DeploymentService) => {
      // given
      const expectedFilters: DeploymentFilter[]  = [{name: 'Confirmed', comp: 'eq', val: 'true'} as DeploymentFilter,
        {name: 'Application', comp: 'eq', val: 'TestApp'} as DeploymentFilter];
      deploymentsComponent.filters = [{name: 'Confirmed', comp: 'eq', val: 'true', type: 'booleanType'} as DeploymentFilter,
        {name: 'Application', comp: 'eq', val: 'TestApp', type: 'StringType'} as DeploymentFilter];
      const deploymentFilters: DeploymentFilterType[] = [{name: 'Application', type: 'StringType'},
        {name: 'Confirmed on', type: 'DateType'}];
      const comparatorOptions: ComparatorFilterOption[] = [{name: 'lt', displayName: '<'},
        {name: 'eq', displayName: 'is'}, {name: 'neq', displayName: 'is not'}];
      spyOn(deploymentService, 'getAllDeploymentFilterTypes').and.returnValue(Observable.of(deploymentFilters));
      spyOn(deploymentService, 'getAllComparatorFilterOptions').and.returnValue(Observable.of(comparatorOptions));
      spyOn(deploymentService, 'getFilteredDeployments').and.returnValue(Observable.of([]));

      // when
      deploymentsComponent.sortDeploymentsBy('d.trackingId');

      // then
      expect(deploymentService.getFilteredDeployments).toHaveBeenCalledWith(JSON.stringify(expectedFilters), 'd.trackingId', 'DESC', 0, 10);

      // when
      deploymentsComponent.sortDeploymentsBy('d.trackingId');

      // then
      expect(deploymentService.getFilteredDeployments).toHaveBeenCalledWith(JSON.stringify(expectedFilters), 'd.trackingId', 'ASC', 0, 10);
  }));

  it('should check permission on showEdit',
    inject([DeploymentsComponent, DeploymentService, ResourceService],
      (deploymentsComponent: DeploymentsComponent, deploymentService: DeploymentService, resourceService: ResourceService) => {
      // given
      deploymentsComponent.deployments = [{id: 1, appServerId: 12, selected: false} as Deployment, {id: 21, appServerId: 22, selected: true} as Deployment];
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
      const deployment: Deployment = {id: 1} as Deployment;
      spyOn(deploymentService, 'confirmDeployment').and.returnValue(Observable.of());
      spyOn(deploymentService, 'getWithActions').and.returnValue(Observable.of(deployment));

      // when
      deploymentsComponent.confirmDeployment(deployment);

      // then
      expect(deploymentService.confirmDeployment).toHaveBeenCalledWith(deployment);
      expect(deploymentService.getWithActions).toHaveBeenCalledWith(deployment.id);
  }));

  it('should reject a deployment and reload it',
    inject([DeploymentsComponent, DeploymentService],
      (deploymentsComponent: DeploymentsComponent, deploymentService: DeploymentService) => {
      // given
      const deployment: Deployment = {id: 2} as Deployment;
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
      const deployment: Deployment = {id: 3} as Deployment;
      spyOn(deploymentService, 'cancelDeployment').and.returnValue(Observable.of());
      spyOn(deploymentService, 'getWithActions').and.returnValue(Observable.of(deployment));

      // when
      deploymentsComponent.cancelDeployment(deployment);

      // then
      expect(deploymentService.cancelDeployment).toHaveBeenCalledWith(deployment.id);
      expect(deploymentService.getWithActions).toHaveBeenCalledWith(deployment.id);
  }));

  it('should invoke the right deploymentService methods with right arguments on changeDeploymentDate',
    inject([DeploymentsComponent, DeploymentService],
      (deploymentsComponent: DeploymentsComponent, deploymentService: DeploymentService) => {
      // given
      const deployment: Deployment = {id: 3, deploymentDate: 1253765123} as Deployment;
      deploymentsComponent.deployments = [{id: 1, deploymentDate: 121212121} as Deployment,
        {id: 3, deploymentDate: 121212111} as Deployment, {id: 5, deploymentDate: 121212122} as Deployment];

      spyOn(deploymentService, 'setDeploymentDate').and.returnValue(Observable.of());
      spyOn(deploymentService, 'getWithActions').and.returnValue(Observable.of(deployment));

      // when
      deploymentsComponent.changeDeploymentDate(deployment);

      // then
      expect(deploymentService.setDeploymentDate).toHaveBeenCalledWith(deployment.id, deployment.deploymentDate);
      expect(deploymentService.getWithActions).toHaveBeenCalledWith(deployment.id);
      expect(deploymentsComponent.deployments[1].deploymentDate).toEqual(deployment.deploymentDate);
  }));

});
