import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { AuthService } from './auth.service';
import { provideHttpClient } from '@angular/common/http';

describe('AuthService', () => {
  let authService: AuthService;
  let httpTestingController: HttpTestingController;
  let API_URL: string;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [],
      providers: [AuthService, provideHttpClient(), provideHttpClientTesting()],
    });
    authService = TestBed.inject(AuthService);
    httpTestingController = TestBed.inject(HttpTestingController);
    API_URL = `${authService.getBaseUrl()}/permissions/restrictions/ownRestrictions/`;
  });

  afterEach(() => {
    httpTestingController.verify();
  });

  it('should reload permissions', () => {
    const req = httpTestingController.expectOne(API_URL);
    expect(req.request.method).toBe('GET');
    authService.refreshData();

    const req2 = httpTestingController.match(API_URL);
    expect(req.request.method).toBe('GET');
  });

  it('should return actions for a permission', () => {
    const permissionName = 'examplePermission';
    const CREATE = 'CREATE';
    const READ = 'READ';

    const req = httpTestingController.expectOne(API_URL);
    expect(req.request.method).toBe('GET');
    req.flush([
      { permission: { name: permissionName }, action: READ },
      { permission: { name: permissionName }, action: CREATE },
      { permission: { name: 'secondPermission' }, action: CREATE },
      { permission: { name: 'thirdPermission' }, action: CREATE },
    ]);

    expect(authService.getActionsForPermission(permissionName)).toEqual([READ, CREATE]);
  });

  it('should not return actions for a missing permission', () => {
    const permissionName = 'examplePermission';
    const action = 'CREATE';

    const req = httpTestingController.expectOne(API_URL);
    expect(req.request.method).toBe('GET');
    req.flush([
      { permission: { name: 'firstPermiison' }, action: action },
      { permission: { name: 'secondPermission' }, action: action },
      { permission: { name: 'thirdPermission' }, action: action },
    ]);

    expect(authService.getActionsForPermission(permissionName)).toEqual([]);
  });

  it('should return true if the user has the specified permission and action', () => {
    const permissionName = 'examplePermission';
    const action = 'CREATE';

    const req = httpTestingController.expectOne(API_URL);
    expect(req.request.method).toBe('GET');
    req.flush([
      { permission: { name: permissionName }, action: 'READ' },
      { permission: { name: permissionName }, action: action },
    ]);

    expect(authService.hasPermission(permissionName, action)).toBeTrue();
  });

  it('should return true if the user has the specified permission and ALL action', () => {
    const permissionName = 'examplePermission';
    const action = 'CREATE';

    const req = httpTestingController.expectOne(API_URL);
    expect(req.request.method).toBe('GET');
    req.flush([{ permission: { name: permissionName }, action: 'ALL' }]);

    expect(authService.hasPermission(permissionName, action)).toBeTrue();
  });

  it('should return false if the user has the specified permission and but not action', () => {
    const permissionName = 'examplePermission';
    const action = 'CREATE';

    const req = httpTestingController.expectOne(API_URL);
    expect(req.request.method).toBe('GET');
    req.flush([{ permission: { name: permissionName }, action: 'READ' }]);

    expect(authService.hasPermission(permissionName, action)).toBeFalse();
  });

  it("should return false if the user doesn't have the specified permission", () => {
    const permissionName = 'examplePermission';
    const action = 'CREATE';

    const req = httpTestingController.expectOne(API_URL);
    expect(req.request.method).toBe('GET');
    req.flush([{ permission: { name: 'otherPermission' }, action: 'READ' }]);

    expect(authService.hasPermission(permissionName, action)).toBeFalse();
  });
});
