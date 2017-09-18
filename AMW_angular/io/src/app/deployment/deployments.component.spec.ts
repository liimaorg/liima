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
import { DeploymentService } from './deployment.service';
import { DeploymentFilterType } from './deployment-filter-type';

@Component({
  template: ''
})
class DummyComponent {
}

describe('DeploymentsComponent', () => {
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
    }));

  it('should enhance filters with the right comparator options on ngOnInit',
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
    }));

});
