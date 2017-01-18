import { inject, TestBed } from '@angular/core/testing';
import { BaseRequestOptions, Response, ResponseOptions, Http } from '@angular/http';
import { MockBackend } from '@angular/http/testing';
import { ResourceService } from './resource.service';

describe('ResourceService', () => {
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
      ResourceService
    ]
  }));

  it('should have a getAll method', inject([ResourceService], (resourceService: ResourceService) => {
    expect(resourceService.getAll()).toBeDefined();
  }));

  it('should request data from the right endpoint when getAll is called', inject([ResourceService, MockBackend, Http], (resourceService: ResourceService, mockBackend: MockBackend, http: Http) => {
    // given
    mockBackend.connections.subscribe(connection => {
      expect(connection.request.url).toMatch('http://localhost:8080/AMW_rest/resources/resources');
      let mockResponse = new Response(new ResponseOptions({
        body: [{id: 1}]
      }));
      connection.mockRespond(mockResponse);
    });
    // when then
    resourceService.getAll().subscribe(response => {
      expect(response).toBeDefined();
      expect(response[0].id).toEqual(1);
    });
  }));

  it('should request data from the right endpoint when getInRelease is called', inject([ResourceService, MockBackend, Http], (resourceService: ResourceService, mockBackend: MockBackend, http: Http) => {
    // given
    mockBackend.connections.subscribe(connection => {
      expect(connection.request.url).toContain('http://localhost:8080/AMW_rest/resources/resources/testGroup/testRelease?env=Global&appsOnly=true');
      let mockResponse = new Response(new ResponseOptions({
        body: [{name: 'testApp'}]
      }));
      connection.mockRespond(mockResponse);
    });
    // when then
    resourceService.getInRelease('testGroup', 'testRelease', true).subscribe(response => {
      expect(response).toBeDefined();
      expect(response[0].name).toEqual('testApp');
    });
  }));

  it('should request data from the right endpoint when getRuntime is called', inject([ResourceService, MockBackend, Http], (resourceService: ResourceService, mockBackend: MockBackend, http: Http) => {
    // given
    mockBackend.connections.subscribe(connection => {
      expect(connection.request.url).toContain('http://localhost:8080/AMW_rest/resources/resources/testGroup/testRelease/relations?type=RUNTIME');
      let mockResponse = new Response(new ResponseOptions({
        body: [{identifier: 'EAP6'}]
      }));
      connection.mockRespond(mockResponse);
    });
    // when then
    resourceService.getRuntime('testGroup', 'testRelease').subscribe(response => {
      expect(response).toBeDefined();
      expect(response[0].identifier).toEqual('EAP6');
    });
  }));

  it('should request data from the right endpoint when getAppversion is called', inject([ResourceService, MockBackend, Http], (resourceService: ResourceService, mockBackend: MockBackend, http: Http) => {
    // given
    mockBackend.connections.subscribe(connection => {
      expect(connection.request.url).toContain('http://localhost:8080/AMW_rest/resources/resources/testGroup/testRelease/appversions/?context=1&context=2');
      let mockResponse = new Response(new ResponseOptions({
        body: [{version: 0.1, mavenVersion: 1.0}, {version: 0.2, mavenVersion: 1.2}]
      }));
      connection.mockRespond(mockResponse);
    });
    // when then
    resourceService.getAppversion('testGroup', 'testRelease', [1, 2]).subscribe(response => {
      expect(response).toBeDefined();
    });
  }));

  it('should correctly map the returned data from the endpoint when getAppversion is called', inject([ResourceService, MockBackend, Http], (resourceService: ResourceService, mockBackend: MockBackend, http: Http) => {
    // given
    mockBackend.connections.subscribe(connection => {
      expect(connection.request.url).toContain('http://localhost:8080/AMW_rest/resources/resources/testGroup/testRelease/appversions/?context=1&context=2');
      let mockResponse = new Response(new ResponseOptions({
        body: [{version: 0.1, mavenVersion: 1.0}, {version: 0.2, mavenVersion: 1.2}]
      }));
      connection.mockRespond(mockResponse);
    });
    // when then
    resourceService.getAppversion('testGroup', 'testRelease', [1, 2]).subscribe(response => {
      expect(response[0].version).toEqual(0.1);
      expect(response.find(item => item.hasOwnProperty('mavenVersion'))).toBeFalsy();
    });
  }));

});
