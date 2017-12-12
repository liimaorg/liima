import { inject, TestBed } from '@angular/core/testing';
import { BaseRequestOptions, Response, ResponseOptions, Http, RequestMethod, Headers } from '@angular/http';
import { MockBackend } from '@angular/http/testing';
import { PermissionService } from './permission.service';
import { Restriction } from './restriction';

describe('PermissiontService', () => {
  beforeEach(() => TestBed.configureTestingModule({
    providers: [
      BaseRequestOptions,
      MockBackend,
      {
        provide: Http,
        useFactory(backend: MockBackend, defaultOptions: BaseRequestOptions) {
          return new Http(backend, defaultOptions);
        },
        deps: [MockBackend, BaseRequestOptions]
      },
      PermissionService
    ]
  }));

  it('should have a getAllRoleNames method',
    inject([PermissionService], (permissionService: PermissionService) => {
    expect(permissionService.getAllRoleNames()).toBeDefined();
  }));

  it('should request data from the right endpoint when getAllRoleNames is called',
    inject([PermissionService, MockBackend], (permissionService: PermissionService, mockBackend: MockBackend) => {
    // given
    mockBackend.connections.subscribe((connection) => {
      expect(connection.request.method).toBe(RequestMethod.Get);
      expect(connection.request.url).toMatch('/AMW_rest/resources/permissions/restrictions/roleNames');
      const mockResponse = new Response(new ResponseOptions({body: ['viewer']}));
      connection.mockRespond(mockResponse);
    });
    // when then
    permissionService.getAllRoleNames().subscribe((response) => {
      expect(response).toEqual(['viewer']);
    });
  }));

  it('should request data from the right endpoint when getAllUserRestrictionNames is called',
    inject([PermissionService, MockBackend], (permissionService: PermissionService, mockBackend: MockBackend) => {
      // given
      mockBackend.connections.subscribe((connection) => {
        expect(connection.request.method).toBe(RequestMethod.Get);
        expect(connection.request.url).toMatch('/AMW_rest/resources/permissions/restrictions/userRestrictionNames');
        const mockResponse = new Response(new ResponseOptions({body: ['testerli']}));
        connection.mockRespond(mockResponse);
      });
      // when then
      permissionService.getAllUserRestrictionNames().subscribe((response) => {
        expect(response).toEqual(['testerli']);
      });
    }));

  it('should request data from the right endpoint when getAllUserPermissionNames is called',
    inject([PermissionService, MockBackend], (permissionService: PermissionService, mockBackend: MockBackend) => {
      // given
      mockBackend.connections.subscribe((connection) => {
        expect(connection.request.method).toBe(RequestMethod.Get);
        expect(connection.request.url).toMatch('/AMW_rest/resources/permissions/restrictions/permissionEnumValues');
        const mockResponse = new Response(new ResponseOptions({body: ['RESOURCE']}));
        connection.mockRespond(mockResponse);
      });
      // when then
      permissionService.getAllPermissionEnumValues().subscribe((response) => {
        expect(response).toEqual(['RESOURCE']);
      });
    }));

  it('should request data from the right endpoint when getRoleWithRestrictions is called',
    inject([PermissionService, MockBackend], (permissionService: PermissionService, mockBackend: MockBackend) => {
      // given
      mockBackend.connections.subscribe((connection) => {
        expect(connection.request.method).toBe(RequestMethod.Get);
        expect(connection.request.url).toMatch('/AMW_rest/resources/permissions/restrictions/roles/viewer');
        const mockResponse = new Response(new ResponseOptions({body: [{id: 1, roleName: 'viewer'},
          {id: 2, roleName: 'viewer'}]}));
        connection.mockRespond(mockResponse);
      });
      // when then
      permissionService.getRoleWithRestrictions('viewer').subscribe((response) => {
        expect(response).toEqual([{id: 1, roleName: 'viewer'}, {id: 2, roleName: 'viewer'}]);
      });
    }));

  it('should request data from the right endpoint when getUserWithRestrictions is called',
    inject([PermissionService, MockBackend], (permissionService: PermissionService, mockBackend: MockBackend) => {
      // given
      mockBackend.connections.subscribe((connection) => {
        expect(connection.request.method).toBe(RequestMethod.Get);
        expect(connection.request.url).toMatch('/AMW_rest/resources/permissions/restrictions/users/tester');
        const mockResponse = new Response(new ResponseOptions({body: [{id: 1, userName: 'tester'},
          {id: 2, userName: 'tester'}]}));
        connection.mockRespond(mockResponse);
      });
      // when then
      permissionService.getUserWithRestrictions('tester').subscribe((response) => {
        expect(response).toEqual([ { id: 1, userName: 'tester' }, { id: 2, userName: 'tester' } ]);
      });
    }));

  it('should invoke the right endpoint when removeRestriction is called',
    inject([PermissionService, MockBackend], (permissionService: PermissionService, mockBackend: MockBackend) => {
      // given
      mockBackend.connections.subscribe((connection) => {
        expect(connection.request.method).toBe(RequestMethod.Delete);
        expect(connection.request.url).toMatch('/AMW_rest/resources/permissions/restrictions/23');
        const mockResponse = new Response(new ResponseOptions({}));
        connection.mockRespond(mockResponse);
      });
      // when then
      permissionService.removeRestriction(23).subscribe((response) => {
        expect(response).toEqual({});
      });
    }));

  it('should invoke the right endpoint when updateRestriction is called',
    inject([PermissionService, MockBackend], (permissionService: PermissionService, mockBackend: MockBackend) => {
    // given
    mockBackend.connections.subscribe((connection) => {
      expect(connection.request.method).toBe(RequestMethod.Put);
      expect(connection.request.url).toMatch('/AMW_rest/resources/permissions/restrictions/2');
      const mockResponse = new Response(new ResponseOptions({status: 200}));
      connection.mockRespond(mockResponse);
    });
    // when then
    permissionService.updateRestriction({ id: 2} as Restriction).subscribe((response) => {
      expect(response).toEqual({});
    });
  }));

  it('should invoke the right endpoints when createRestriction is called',
    inject([PermissionService, MockBackend], (permissionService: PermissionService, mockBackend: MockBackend) => {
    // given
    mockBackend.connections.subscribe((connection) => {
      if (connection.request.method === RequestMethod.Post) {
        expect(connection.request.url).toMatch('/AMW_rest/resources/permissions/restrictions/');
        const locationHeaders = new Headers();
        locationHeaders.set('Location', '/permissions/restrictions/8');
        const mockResponse = new Response(new ResponseOptions({status: 201, headers: locationHeaders}));
        connection.mockRespond(mockResponse);
      } else {
        expect(connection.request.method).toBe(RequestMethod.Get);
        expect(connection.request.url).toMatch('/AMW_rest/resources/permissions/restrictions/8');
        const mockResponse = new Response(new ResponseOptions({body: {id: 8, roleName: 'TESTER'}}));
        connection.mockRespond(mockResponse);
      }
    });
    // when then
    permissionService.createRestriction({roleName: 'TESTER'} as Restriction, false).subscribe((response) => {
      expect(response).toEqual({id: 8, roleName: 'TESTER'});
    });
  }));

});
