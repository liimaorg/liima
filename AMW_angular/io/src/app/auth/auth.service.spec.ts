import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { AuthService } from './auth.service';
import { provideHttpClient } from '@angular/common/http';
import { Action, Restriction } from 'src/app/auth/restriction';
import { EnvironmentService } from '../deployment/environment.service';
import { signal } from '@angular/core';
import { Environment } from '../deployment/environment';

describe('AuthService', () => {
  let authService: AuthService;
  let httpTestingController: HttpTestingController;
  let API_URL: string;
  let environmentService: EnvironmentService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [],
      providers: [
        AuthService,
        provideHttpClient(),
        provideHttpClientTesting(),
        {
          provide: EnvironmentService,
          useValue: {
            contexts: signal<Environment[]>([]),
          },
        },
      ],
    });
    authService = TestBed.inject(AuthService);
    httpTestingController = TestBed.inject(HttpTestingController);
    environmentService = TestBed.inject(EnvironmentService);
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

    expect(authService.hasPermission(permissionName, action)).toBe(true);
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

    expect(authService.hasPermission(permissionName, action)).toBe(true);
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

    expect(authService.hasPermission(permissionName, action)).toBe(false);
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

    expect(authService.hasPermission(permissionName, action)).toBe(false);
  });

  describe('hasPermission', () => {
    const permissionName = 'RESOURCE';
    const action: Action = 'UPDATE';
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

      expect(authService.hasPermission(permissionName, action, resourceTypeName, resourceGroupId)).toBe(true);
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

      expect(authService.hasPermission(permissionName, action, resourceTypeName, resourceGroupId)).toBe(true);
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

      expect(authService.hasPermission(permissionName, action, resourceTypeName, resourceGroupId)).toBe(true);
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

      expect(authService.hasPermission(permissionName, action, resourceTypeName, resourceGroupId)).toBe(true);
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

      expect(authService.hasPermission(permissionName, action, resourceTypeName, resourceGroupId)).toBe(false);
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

      expect(authService.hasPermission(permissionName, action, resourceTypeName, resourceGroupId)).toBe(false);
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

      expect(authService.hasPermission(permissionName, action, resourceTypeName, resourceGroupId)).toBe(false);
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
        expect(authService.hasPermission(permissionName, action, defaultTypeName, resourceGroupId)).toBe(true);
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
        expect(authService.hasPermission(permissionName, action, nonDefaultTypeName, resourceGroupId)).toBe(false);
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
        expect(authService.hasPermission(permissionName, action, nonDefaultTypeName, resourceGroupId)).toBe(true);
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
        expect(authService.hasPermission(permissionName, action, defaultTypeName, resourceGroupId)).toBe(false);
      });
    });

    describe('with context permissions', () => {
      const permissionName = 'RESOURCE';
      const action: Action = 'UPDATE';
      const resourceTypeName = 'MyResourceType';
      const resourceGroupId = 123;
      const baseRestriction: Omit<Restriction, 'contextName' | 'action' | 'permission'> = {
        id: null,
        roleName: null,
        userName: null,
        resourceTypeName: null,
        resourceGroupId: null,
        resourceTypePermission: 'ANY',
      };

      beforeEach(() => {
        // Setup mock environments
        const mockEnvironments: Environment[] = [
          {
            id: 1,
            name: 'DEV',
            parentName: 'GLOBAL',
            parentId: null,
            nameAlias: null,
            selected: false,
            disabled: false,
          },
          {
            id: 2,
            name: 'TEST',
            parentName: 'GLOBAL',
            parentId: null,
            nameAlias: null,
            selected: false,
            disabled: false,
          },
          {
            id: 3,
            name: 'PROD',
            parentName: 'GLOBAL',
            parentId: null,
            nameAlias: null,
            selected: false,
            disabled: false,
          },
          { id: 4, name: 'D', parentName: 'DEV', parentId: null, nameAlias: null, selected: false, disabled: false },
        ];
        (environmentService.contexts as any).set(mockEnvironments);
      });

      it('should return true if context is null', () => {
        const req = httpTestingController.expectOne(API_URL);
        req.flush([{ ...baseRestriction, permission: { name: permissionName }, action: action, contextName: 'DEV' }]);
        expect(authService.hasPermission(permissionName, action, resourceTypeName, resourceGroupId, null)).toBe(true);
      });

      it('should return true if restriction contextName is null', () => {
        const req = httpTestingController.expectOne(API_URL);
        req.flush([{ ...baseRestriction, permission: { name: permissionName }, action: action, contextName: null }]);
        expect(authService.hasPermission(permissionName, action, resourceTypeName, resourceGroupId, 'DEV')).toBe(true);
      });

      it('should return true if restriction contextName is GLOBAL', () => {
        const req = httpTestingController.expectOne(API_URL);
        req.flush([
          { ...baseRestriction, permission: { name: permissionName }, action: action, contextName: 'GLOBAL' },
        ]);
        expect(authService.hasPermission(permissionName, action, resourceTypeName, resourceGroupId, 'DEV')).toBe(true);
      });

      it('should return true if restriction contextName is GLOBAL with sub-context', () => {
        const req = httpTestingController.expectOne(API_URL);
        req.flush([
          { ...baseRestriction, permission: { name: permissionName }, action: action, contextName: 'GLOBAL' },
        ]);
        expect(authService.hasPermission(permissionName, action, resourceTypeName, resourceGroupId, 'B')).toBe(true);
      });

      it('should return true if contextName matches', () => {
        const req = httpTestingController.expectOne(API_URL);
        req.flush([{ ...baseRestriction, permission: { name: permissionName }, action: action, contextName: 'DEV' }]);
        expect(authService.hasPermission(permissionName, action, resourceTypeName, resourceGroupId, 'DEV')).toBe(true);
      });

      it('should return true if restriction contextName is a parent of the context', () => {
        const req = httpTestingController.expectOne(API_URL);
        req.flush([{ ...baseRestriction, permission: { name: permissionName }, action: action, contextName: 'DEV' }]);
        expect(authService.hasPermission(permissionName, action, resourceTypeName, resourceGroupId, 'D')).toBe(true);
      });

      it('should return false if context does not match', () => {
        const req = httpTestingController.expectOne(API_URL);
        req.flush([{ ...baseRestriction, permission: { name: permissionName }, action: action, contextName: 'PROD' }]);
        expect(authService.hasPermission(permissionName, action, resourceTypeName, resourceGroupId, 'DEV')).toBe(false);
      });

      it('should return false if context is a parent of restriction context', () => {
        const req = httpTestingController.expectOne(API_URL);
        req.flush([{ ...baseRestriction, permission: { name: permissionName }, action: action, contextName: 'D' }]);
        expect(authService.hasPermission(permissionName, action, resourceTypeName, resourceGroupId, 'DEV')).toBe(false);
      });
    });
  });
});
