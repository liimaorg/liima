import { TestBed } from '@angular/core/testing';
import { PermissionService } from './permission.service';
import { Restriction } from 'src/app/auth/restriction';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';

describe('PermissionService', () => {
  let service: PermissionService;

  let httpTestingController: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [],
      providers: [PermissionService, provideHttpClient(withInterceptorsFromDi()), provideHttpClientTesting()],
    });
    service = TestBed.inject(PermissionService);

    httpTestingController = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTestingController.verify();
  });

  it('should create service', () => {
    expect(service).toBeTruthy();
  });

  it('should getAllRoleNames from the correct endpoint', () => {
    service.getAllRoleNames().subscribe((roleNames) => {
      expect(roleNames).toEqual(['admin', 'user', 'guest']);
    });

    const req = httpTestingController.expectOne('/AMW_rest/resources/permissions/restrictions/roleNames');

    expect(req.request.method).toEqual('GET');
    req.flush(['admin', 'user', 'guest']);
  });

  it('should request data from the right endpoint when getAllUserRestrictionNames is called', () => {
    service.getAllUserRestrictionNames().subscribe((response) => {
      expect(response).toEqual(['testerli']);
    });

    const req = httpTestingController.expectOne('/AMW_rest/resources/permissions/restrictions/userRestrictionNames');

    expect(req.request.method).toEqual('GET');
    req.flush(['testerli']);
  });

  it('should request data from the right endpoint when getAllUserPermissionNames is called', () => {
    const permission = {
      name: 'permission',
      old: false,
      longName: 'longname',
    };

    service.getAllPermissionEnumValues().subscribe((response) => {
      expect(response).toEqual([permission]);
    });

    const req = httpTestingController.expectOne('/AMW_rest/resources/permissions/restrictions/permissionEnumValues');

    req.flush([permission]);
  });

  it('should request data from the right endpoint when getRoleWithRestrictions is called', () => {
    // given

    const mockeResponse = [{ id: 1, roleName: 'viewer' } as Restriction, { id: 2, roleName: 'viewer' } as Restriction];

    service.getRoleWithRestrictions('viewer').subscribe((response) => {
      expect(response).toEqual(mockeResponse);
    });

    const req = httpTestingController.expectOne('/AMW_rest/resources/permissions/restrictions/roles/viewer');

    req.flush(mockeResponse);
  });

  it('should request data from the right endpoint when getUserWithRestrictions is called', () => {
    // given
    const mockeResponse = [{ id: 1, roleName: 'viewer' } as Restriction, { id: 2, roleName: 'viewer' } as Restriction];

    service.getUserWithRestrictions('tester').subscribe((response) => {
      expect(response).toEqual(mockeResponse);
    });

    const req = httpTestingController.expectOne('/AMW_rest/resources/permissions/restrictions/users/tester');

    req.flush(mockeResponse);
  });

  it('should invoke the right endpoint when removeRestriction is called', () => {
    service.removeRestriction(23).subscribe((response) => {
      expect(response).toEqual({});
    });
    const req = httpTestingController.expectOne('/AMW_rest/resources/permissions/restrictions/23');

    expect(req.request.method).toEqual('DELETE');
    req.flush({});
  });

  it('should invoke the right endpoint when updateRestriction is called', () => {
    service.updateRestriction({ id: 2 } as Restriction).subscribe((response) => {
      expect(response).toEqual({});
    });

    const req = httpTestingController.expectOne('/AMW_rest/resources/permissions/restrictions/2');
    expect(req.request.method).toEqual('PUT');
    req.flush({});
  });

  // see https://github.com/angular/angular/issues/25047
  // activate the test as soon as the issue is resolved (if ever)
  // or do a http post request without query params... and change the api accordingly
  xit('should invoke the right endpoints when createRestriction is called', () => {
    service.createRestriction({ roleName: 'TESTER' } as Restriction, false).subscribe((response) => {
      expect(response).toEqual({ id: 8, roleName: 'TESTER' } as Restriction);
    });
    const req = httpTestingController.expectOne('/AMW_rest/resources/permissions/restrictions/');

    expect(req.request.method).toEqual('POST');
    req.flush({ id: 8, roleName: 'TESTER' } as Restriction);
    httpTestingController.expectOne('/AMW_rest/resourcesnull');
  });
});
