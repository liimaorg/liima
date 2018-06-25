import { Component } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { inject, TestBed } from '@angular/core/testing';
import { BaseRequestOptions, ConnectionBackend, Http } from '@angular/http';
import { MockBackend } from '@angular/http/testing';
import { CommonModule } from '@angular/common';
import { RouterTestingModule } from '@angular/router/testing';
import { AppState } from '../app.service';
import { Deployment } from './deployment';
import { DeploymentService } from './deployment.service';
import { DeploymentsEditModalComponent } from './deployments-edit-modal.component';
import * as moment from 'moment';

@Component({
  template: ''
})
class DummyComponent {
}

describe('DeploymentsEditModalComponent (with query params)', () => {
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
        provide: ActivatedRoute
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
      DeploymentsEditModalComponent,
      AppState
    ],
    declarations: [DummyComponent],
  }));

  it('should log unknown edit actions on doEdit',
    inject([DeploymentsEditModalComponent], (deploymentsEditModalComponent: DeploymentsEditModalComponent) => {
      // given
      deploymentsEditModalComponent.editActions = ['Change date', 'Confirm', 'Reject', 'Cancel'];
      deploymentsEditModalComponent.selectedEditAction = 'test';
      deploymentsEditModalComponent.deployments = [{id: 1, selected: true} as Deployment];
      spyOn(console, 'error');

      // when
      deploymentsEditModalComponent.doEdit();

      // then
      expect(console.error).toHaveBeenCalled();
  }));

  it('should apply date for confirmation',
    inject([DeploymentsEditModalComponent], (deploymentsEditModalComponent: DeploymentsEditModalComponent) => {
      // given
      const newDeploymentDate: string = '30.11.2017 09:19';
      const expectedDeploymentDate: number = moment(newDeploymentDate, 'DD.MM.YYYY HH:mm').valueOf();

      deploymentsEditModalComponent.editActions = ['Change date', 'Confirm', 'Reject', 'Cancel'];
      deploymentsEditModalComponent.deploymentDate = newDeploymentDate;
      deploymentsEditModalComponent.selectedEditAction = 'Confirm';
      deploymentsEditModalComponent.deployments = [{id: 1, selected: true, deploymentDate: 5555} as Deployment,
        {id: 1, selected: true, deploymentDate: 6666} as Deployment];
      spyOn(console, 'error');

      // when
      deploymentsEditModalComponent.doEdit();

      // then
      const deployment1: Deployment = deploymentsEditModalComponent.deployments[0];
      const deployment2: Deployment = deploymentsEditModalComponent.deployments[1];
      expect(deployment1.deploymentDate).toEqual(expectedDeploymentDate);
      expect(deployment2.deploymentDate).toEqual(expectedDeploymentDate);
    }));

  it('should clear data after doEdit()',
    inject([DeploymentsEditModalComponent], (deploymentsEditModalComponent: DeploymentsEditModalComponent) => {
      // given
      const newDeploymentDate: string = '30.11.2017 09:19';

      deploymentsEditModalComponent.editActions = ['Change date', 'Confirm', 'Reject', 'Cancel'];
      deploymentsEditModalComponent.deploymentDate = newDeploymentDate;
      deploymentsEditModalComponent.selectedEditAction = 'Confirm';
      deploymentsEditModalComponent.deployments = [{id: 1, selected: true, deploymentDate: 5555} as Deployment,
        {id: 1, selected: true, deploymentDate: 6666} as Deployment];

      // when
      deploymentsEditModalComponent.doEdit();

      // then
      expect(deploymentsEditModalComponent.confirmationAttributes).toEqual({} as Deployment);
      expect(deploymentsEditModalComponent.selectedEditAction).toEqual('');
      expect(deploymentsEditModalComponent.deploymentDate).toEqual('');
  }));

});
