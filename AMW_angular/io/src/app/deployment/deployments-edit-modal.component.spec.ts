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
import {DeploymentsEditModalComponent} from "./deployments-edit-modal.component";

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
        useFactory: function (backend: ConnectionBackend, defaultOptions: BaseRequestOptions) {
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
      deploymentsEditModalComponent.deployments = [ <Deployment> { id: 1, selected: true } ];
      spyOn(console, 'error');

      // when
      deploymentsEditModalComponent.doEdit();

      // then
      expect(console.error).toHaveBeenCalled();
  }));

});
