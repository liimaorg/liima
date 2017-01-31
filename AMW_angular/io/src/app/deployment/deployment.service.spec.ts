import { inject, TestBed } from '@angular/core/testing';
import { BaseRequestOptions, Response, ResponseOptions, Http } from '@angular/http';
import { MockBackend } from '@angular/http/testing';
import { DeploymentService } from './deployment.service';
import { DeploymentRequest } from './deployment-request';

describe('DeploymentService', () => {
  beforeEach(() => TestBed.configureTestingModule({
    providers: [
      BaseRequestOptions,
      MockBackend,
      {
        provide: Http,
        useFactory: function (backend: MockBackend, defaultOptions: BaseRequestOptions) {
          return new Http(backend, defaultOptions);
        },
        deps: [MockBackend, BaseRequestOptions]
      },
      DeploymentService
    ]
  }));

  it('should have a getAll method', inject([DeploymentService], (deploymentService: DeploymentService) => {
    expect(deploymentService.getAll()).toBeDefined();
  }));

  it('should have a createDeployment method', inject([DeploymentService], (deploymentService: DeploymentService) => {
    let deploymentRequest: DeploymentRequest = <DeploymentRequest>{};
    expect(deploymentService.createDeployment(deploymentRequest)).toBeDefined();
  }));

  it('should request data from the right endpoint when getAll is called', inject([DeploymentService, MockBackend, Http], (deploymentService: DeploymentService, mockBackend: MockBackend, http: Http) => {
    // given
    mockBackend.connections.subscribe((connection) => {
      expect(connection.request.url).toMatch('http://localhost:8080/AMW_rest/resources/deployments');
      let mockResponse = new Response(new ResponseOptions({
        body: [{id: 1}]
      }));
      connection.mockRespond(mockResponse);
    });
    // when then
    deploymentService.getAll().subscribe((response) => {
      expect(response).toEqual([{id: 1}]);
    });
  }));

  it('should request data from the right endpoint when getAllDeploymentParameterKeys is called', inject([DeploymentService, MockBackend, Http], (deploymentService: DeploymentService, mockBackend: MockBackend, http: Http) => {
    // given
    mockBackend.connections.subscribe((connection) => {
      expect(connection.request.url).toMatch('http://localhost:8080/AMW_rest/resources/deployments/deploymentParameterKeys');
      let mockResponse = new Response(new ResponseOptions({
        body: [{key: 1, value: 'test'}]
      }));
      connection.mockRespond(mockResponse);
    });
    // when then
    deploymentService.getAllDeploymentParameterKeys().subscribe((response) => {
      expect(response).toEqual([{key: 1, value: 'test'}]);
    });
  }));

});
