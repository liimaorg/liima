import { inject, TestBed } from '@angular/core/testing';
import { BaseRequestOptions, Response, ResponseOptions, Http } from '@angular/http';
import { MockBackend } from '@angular/http/testing';
import { EnvironmentService } from './environment.service';

describe('EnvironmentService', () => {
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
      EnvironmentService
    ]
  }));

  it('should have a getAll method', inject([EnvironmentService], (environmentService: EnvironmentService) => {
    expect(environmentService.getAll()).toBeDefined();
  }));

  it('should request data from the right endpoint when getAll is called', inject([EnvironmentService, MockBackend, Http], (environmentService: EnvironmentService, mockBackend: MockBackend, http: Http) => {
    // given
    mockBackend.connections.subscribe(connection => {
      expect(connection.request.url).toMatch('http://localhost:8080/AMW_rest/resources/environments');
      let mockResponse = new Response(new ResponseOptions({
        body: [{id: 1, name: 'test'}]
      }));
      connection.mockRespond(mockResponse);
    });
    // when then
    environmentService.getAll().subscribe(response => {
      expect(response).toEqual([{id: 1, name: 'test'}]);
    });
  }));

});
