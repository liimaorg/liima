import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { AuthService } from './auth.service';

describe('AuthService', () => {
  let authService: AuthService;
  let httpTestingController: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [AuthService],
    });
    authService = TestBed.inject(AuthService);
    httpTestingController = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTestingController.verify();
  });

  it('should return true if the user has the specified permission and action', () => {
    const permissionName = 'examplePermission';
    const action = 'CREATE';

    authService.hasPermission(permissionName, action).subscribe((result) => {
      expect(result).toBeTrue();
    });

    const req = httpTestingController.expectOne(`${authService.getBaseUrl()}/permissions/restrictions/ownRestrictions/`);
    expect(req.request.method).toBe('GET');
    req.flush([
      { permission: { name: permissionName }, action: 'READ' },
      { permission: { name: permissionName }, action: action },
    ]);
  });

  it('should return true if the user has the specified permission and ALL action', () => {
    const permissionName = 'examplePermission';
    const action = 'CREATE';

    authService.hasPermission(permissionName, action).subscribe((result) => {
      expect(result).toBeTrue();
    });

    const req = httpTestingController.expectOne(`${authService.getBaseUrl()}/permissions/restrictions/ownRestrictions/`);
    expect(req.request.method).toBe('GET');
    req.flush([
      { permission: { name: permissionName }, action: 'ALL' }
    ]);
  });

  it('should return false if the user has the specified permission and but not action', () => {
    const permissionName = 'examplePermission';
    const action = 'CREATE';

    authService.hasPermission(permissionName, action).subscribe((result) => {
      expect(result).toBeFalse();
    });

    const req = httpTestingController.expectOne(`${authService.getBaseUrl()}/permissions/restrictions/ownRestrictions/`);
    expect(req.request.method).toBe('GET');
    req.flush([
      { permission: { name: permissionName }, action: 'READ' }
    ]);
  });

  it("should return false if the user doesn't have the specified permission", () => {
    const permissionName = 'examplePermission';
    const action = 'CREATE';

    authService.hasPermission(permissionName, action).subscribe((result) => {
      expect(result).toBeFalse();
    });

    const req = httpTestingController.expectOne(`${authService.getBaseUrl()}/permissions/restrictions/ownRestrictions/`);
    expect(req.request.method).toBe('GET');
    req.flush([
      { permission: { name: "otherPermission" }, action: 'READ' }
    ]);
  });
});