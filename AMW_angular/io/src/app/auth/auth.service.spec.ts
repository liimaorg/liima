import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { AuthService } from './auth.service';
import { provideHttpClient } from '@angular/common/http';
import { Action } from 'src/app/auth/restriction';

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
    authService.refreshData();
    const requests = httpTestingController.match(API_URL);
    expect(requests.length).toEqual(2);
  });

  it('should return actions for a permission', () => {
    const permissionName = 'examplePermission';
    const CREATE = 'CREATE';
    const READ = 'READ';

    const baseRestriction = {
      id: null,
      roleName: null,
      userName: null,
      resourceGroupId: null,
      resourceTypeName: null,
      resourceTypePermission: null,
      contextName: null,
    };

    const req = httpTestingController.expectOne(API_URL);
    expect(req.request.method).toBe('GET');
    req.flush([
      { ...baseRestriction, permission: { name: permissionName }, action: READ },
      { ...baseRestriction, permission: { name: permissionName }, action: CREATE },
      { ...baseRestriction, permission: { name: 'secondPermission' }, action: CREATE },
      { ...baseRestriction, permission: { name: 'thirdPermission' }, action: CREATE },
    ]);

    expect(authService.getActionsForPermission(permissionName)).toEqual([READ, CREATE]);
  });

  it('should not return actions for a missing permission', () => {
    const permissionName = 'examplePermission';
    const action = 'CREATE';

    const baseRestriction = {
      id: null,
      roleName: null,
      userName: null,
      resourceGroupId: null,
      resourceTypeName: null,
      resourceTypePermission: null,
      contextName: null,
    };

    const req = httpTestingController.expectOne(API_URL);
    expect(req.request.method).toBe('GET');
    req.flush([
      { ...baseRestriction, permission: { name: 'firstPermiison' }, action: action },
      { ...baseRestriction, permission: { name: 'secondPermission' }, action: action },
      { ...baseRestriction, permission: { name: 'thirdPermission' }, action: action },
    ]);

    expect(authService.getActionsForPermission(permissionName)).toEqual([]);
  });

  it('should return true if the user has the specified permission and action', () => {
    const permissionName = 'examplePermission';
    const action = 'CREATE';

    const baseRestriction = {
      id: null,
      roleName: null,
      userName: null,
      resourceGroupId: null,
      resourceTypeName: null,
      resourceTypePermission: 'ANY',
      contextName: null,
    };

    const req = httpTestingController.expectOne(API_URL);
    expect(req.request.method).toBe('GET');
    req.flush([
      { ...baseRestriction, permission: { name: permissionName }, action: 'READ' },
      { ...baseRestriction, permission: { name: permissionName }, action: action },
    ]);

    expect(authService.hasPermission(permissionName, Action[action])).toBeTrue();
  });

  it('should return true if the user has the specified permission and ALL action', () => {
    const permissionName = 'should return true if the user has the specified permission and ALL action';
    const action = 'CREATE';

    const req = httpTestingController.expectOne(API_URL);
    expect(req.request.method).toBe('GET');
    req.flush([
      {
        id: null,
        roleName: null,
        userName: null,
        resourceGroupId: null,
        resourceTypeName: null,
        contextName: null,
        permission: { name: permissionName },
        action: 'ALL',
        resourceTypePermission: 'ANY',
      },
    ]);

    expect(authService.hasPermission(permissionName, Action[action])).toBeTrue();
  });

  it('should return false if the user has the specified permission and but not action', () => {
    const permissionName = 'examplePermission';
    const action = 'CREATE';

    const req = httpTestingController.expectOne(API_URL);
    expect(req.request.method).toBe('GET');
    req.flush([
      {
        id: null,
        roleName: null,
        userName: null,
        resourceGroupId: null,
        resourceTypeName: null,
        contextName: null,
        permission: { name: permissionName },
        action: 'READ',
        resourceTypePermission: 'ANY',
      },
    ]);

    expect(authService.hasPermission(permissionName, Action[action])).toBeFalse();
  });

  it("should return false if the user doesn't have the specified permission", () => {
    const permissionName = 'examplePermission';
    const action = 'CREATE';

    const req = httpTestingController.expectOne(API_URL);
    expect(req.request.method).toBe('GET');
    req.flush([
      {
        id: null,
        roleName: null,
        userName: null,
        resourceGroupId: null,
        resourceTypeName: null,
        resourceTypePermission: 'ANY',
        contextName: null,
        permission: { name: 'otherPermission' },
        action: 'READ',
      },
    ]);

    expect(authService.hasPermission(permissionName, Action[action])).toBeFalse();
  });

  describe('hasPermission', () => {
    const permissionName = 'RESOURCE';
    const action = 'UPDATE';
    const resourceTypeName = 'MyResourceType';
    const resourceGroupId = 123;
    const baseRestriction = {
      id: null,
      roleName: null,
      userName: null,
      contextName: null,
    };

    it('should return true for a perfect match with ANY', () => {
      const req = httpTestingController.expectOne(API_URL);
      req.flush([
        {
          ...baseRestriction,
          permission: { name: permissionName },
          action: action,
          resourceTypeName: null,
          resourceGroupId: resourceGroupId,
          resourceTypePermission: 'ANY',
        },
      ]);

      expect(authService.hasPermission(permissionName, Action[action], resourceTypeName, resourceGroupId)).toBeTrue();
    });

    it('should return true when restriction has null resourceTypeName', () => {
      const req = httpTestingController.expectOne(API_URL);
      req.flush([
        {
          ...baseRestriction,
          permission: { name: permissionName },
          action: action,
          resourceTypeName: null,
          resourceGroupId: resourceGroupId,
          resourceTypePermission: 'ANY',
        },
      ]);

      expect(authService.hasPermission(permissionName, Action[action], resourceTypeName, resourceGroupId)).toBeTrue();
    });

    it('should return true when restriction has null resourceGroupId', () => {
      const req = httpTestingController.expectOne(API_URL);
      req.flush([
        {
          ...baseRestriction,
          permission: { name: permissionName },
          action: action,
          resourceTypeName: resourceTypeName,
          resourceGroupId: null,
          resourceTypePermission: 'ANY',
        },
      ]);

      expect(authService.hasPermission(permissionName, Action[action], resourceTypeName, resourceGroupId)).toBeTrue();
    });

    it('should return true for action ALL', () => {
      const req = httpTestingController.expectOne(API_URL);
      req.flush([
        {
          ...baseRestriction,
          permission: { name: permissionName },
          action: 'ALL',
          resourceTypeName: null,
          resourceGroupId: null,
          resourceTypePermission: 'ANY',
        },
      ]);

      expect(authService.hasPermission(permissionName, Action[action], resourceTypeName, resourceGroupId)).toBeTrue();
    });

    it('should return false if action does not match', () => {
      const req = httpTestingController.expectOne(API_URL);
      req.flush([
        {
          ...baseRestriction,
          permission: { name: permissionName },
          action: 'READ',
          resourceTypeName: null,
          resourceGroupId: null,
          resourceTypePermission: 'ANY',
        },
      ]);

      expect(authService.hasPermission(permissionName, Action[action], resourceTypeName, resourceGroupId)).toBeFalse();
    });

    it('should return false if resourceTypeName does not match', () => {
      const req = httpTestingController.expectOne(API_URL);
      req.flush([
        {
          ...baseRestriction,
          permission: { name: permissionName },
          action: action,
          resourceTypeName: 'AnotherType',
          resourceGroupId: null,
          resourceTypePermission: 'ANY',
        },
      ]);

      expect(authService.hasPermission(permissionName, Action[action], resourceTypeName, resourceGroupId)).toBeFalse();
    });

    it('should return false if resourceGroupId does not match', () => {
      const req = httpTestingController.expectOne(API_URL);
      req.flush([
        {
          ...baseRestriction,
          permission: { name: permissionName },
          action: action,
          resourceTypeName: null,
          resourceGroupId: 456,
          resourceTypePermission: 'ANY',
        },
      ]);

      expect(authService.hasPermission(permissionName, Action[action], resourceTypeName, resourceGroupId)).toBeFalse();
    });

    describe('with resourceTypePermission categories', () => {
      it('should return true for DEFAULT_ONLY with a default type', () => {
        const defaultTypeName = 'APPLICATION';
        const req = httpTestingController.expectOne(API_URL);
        req.flush([
          {
            ...baseRestriction,
            permission: { name: permissionName },
            action: action,
            resourceTypeName: null,
            resourceGroupId: null,
            resourceTypePermission: 'DEFAULT_ONLY',
          },
        ]);
        expect(authService.hasPermission(permissionName, Action[action], defaultTypeName, resourceGroupId)).toBeTrue();
      });

      it('should return false for DEFAULT_ONLY with a non-default type', () => {
        const nonDefaultTypeName = 'MyCustomType';
        const req = httpTestingController.expectOne(API_URL);
        req.flush([
          {
            ...baseRestriction,
            permission: { name: permissionName },
            action: action,
            resourceTypeName: null,
            resourceGroupId: null,
            resourceTypePermission: 'DEFAULT_ONLY',
          },
        ]);
        expect(
          authService.hasPermission(permissionName, Action[action], nonDefaultTypeName, resourceGroupId),
        ).toBeFalse();
      });

      it('should return true for NON_DEFAULT_ONLY with a non-default type', () => {
        const nonDefaultTypeName = 'MyCustomType';
        const req = httpTestingController.expectOne(API_URL);
        req.flush([
          {
            ...baseRestriction,
            permission: { name: permissionName },
            action: action,
            resourceTypeName: null,
            resourceGroupId: null,
            resourceTypePermission: 'NON_DEFAULT_ONLY',
          },
        ]);
        expect(
          authService.hasPermission(permissionName, Action[action], nonDefaultTypeName, resourceGroupId),
        ).toBeTrue();
      });

      it('should return false for NON_DEFAULT_ONLY with a default type', () => {
        const defaultTypeName = 'APPLICATION';
        const req = httpTestingController.expectOne(API_URL);
        req.flush([
          {
            ...baseRestriction,
            permission: { name: permissionName },
            action: action,
            resourceTypeName: null,
            resourceGroupId: null,
            resourceTypePermission: 'NON_DEFAULT_ONLY',
          },
        ]);
        expect(authService.hasPermission(permissionName, Action[action], defaultTypeName, resourceGroupId)).toBeFalse();
      });
    });
  });
});
