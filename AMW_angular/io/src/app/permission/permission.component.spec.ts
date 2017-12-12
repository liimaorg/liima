import { Component } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { inject, TestBed } from '@angular/core/testing';
import { BaseRequestOptions, ConnectionBackend, Http } from '@angular/http';
import { MockBackend } from '@angular/http/testing';
import { CommonModule } from '@angular/common';
import { RouterTestingModule } from '@angular/router/testing';
import { PermissionComponent } from './permission.component';
import { PermissionService } from './permission.service';
import { EnvironmentService } from '../deployment/environment.service';
import { ResourceService } from '../resource/resource.service';
import { Environment } from '../deployment/environment';
import { AppState } from '../app.service';
import { Restriction } from './restriction';
import { Observable } from 'rxjs';
import { Permission } from './permission';

@Component({
  template: ''
})
class DummyComponent {
}

describe('PermissionComponent without any params (default: type Role)', () => {
  // provide our implementations or mocks to the dependency injector
  beforeEach(() => TestBed.configureTestingModule({
    imports: [
      CommonModule,
      RouterTestingModule.withRoutes([
        {path: 'permission', component: DummyComponent}
      ])
    ],
    providers: [
      BaseRequestOptions,
      MockBackend,
      {
        provide: Http,
        useFactory: function (backend: ConnectionBackend, defaultOptions: BaseRequestOptions) {
          return new Http(backend, defaultOptions);
        },
        deps: [MockBackend, BaseRequestOptions]
      },
      EnvironmentService,
      PermissionService,
      ResourceService,
      PermissionComponent,
      AppState,
    ],
    declarations: [DummyComponent],
  }));

  it('should have default data',
    inject([PermissionComponent], (permissionComponent: PermissionComponent) => {
    // given when then
    expect(permissionComponent.roleNames).toEqual([]);
    expect(permissionComponent.userNames).toEqual([]);
    expect(permissionComponent.permissions).toEqual([]);
    expect(permissionComponent.environments).toEqual([ { id: null, name: null, parent: 'All', selected: false } ]);
    expect(permissionComponent.resourceGroups).toEqual([]);
    expect(permissionComponent.resourceTypes).toEqual([ { id: null, name: null } ]);
    expect(permissionComponent.restrictionType).toEqual('role');
  }));

  it('should invoke some services on ngOnInt',
    inject([PermissionComponent, PermissionService, EnvironmentService, ResourceService],
      (permissionComponent: PermissionComponent, permissionService: PermissionService,
       environmentService: EnvironmentService, resourceService: ResourceService) => {
    // given
    let permissions: Permission[] = [ {name: 'RESOURCE', old: false}, {name: 'RESOURCE_TYPE', old: false}];
    let environments: Environment[] = [ <Environment> { id: 1, name: 'U', parent: 'Dev' },
      <Environment> { id: 2, name: 'V', parent: 'Dev' },
      <Environment> { id: 3, name: 'T', parent: 'Test' } ];
    spyOn(permissionService, 'getAllPermissionEnumValues').and.returnValue(Observable.of(permissions));
    spyOn(environmentService, 'getAllIncludingGroups').and.returnValue(Observable.of(environments));
    spyOn(resourceService, 'getAllResourceGroups').and.callThrough();
    spyOn(resourceService, 'getAllResourceTypes').and.callThrough();
    // when
    permissionComponent.ngOnInit();
    // then
    expect(permissionComponent.delegationMode).toBeFalsy();
    expect(permissionComponent.restrictionType).toEqual('role');
    expect(permissionService.getAllPermissionEnumValues).toHaveBeenCalled();
    expect(environmentService.getAllIncludingGroups).toHaveBeenCalled();
    expect(resourceService.getAllResourceGroups).toHaveBeenCalled();
    expect(resourceService.getAllResourceTypes).toHaveBeenCalled();
    expect(permissionComponent.permissions).toEqual(permissions);
    expect(permissionComponent.restriction).toBeNull();
    expect(permissionComponent.groupedEnvironments['All']).toContain({ id: null, name: null, parent: 'All', selected: false });
    expect(permissionComponent.groupedEnvironments['Dev']).toContain({ id: 1, name: 'U', parent: 'Dev', selected: false },
      { id: 2, name: 'V', parent: 'Dev', selected: false });
    expect(permissionComponent.groupedEnvironments['Test']).toContain({ id: 3, name: 'T', parent: 'Test', selected: false });
  }));

  it('should invoke PermissionService and sort the Restrictions by Permission.name, action on changeRole if selected Role exists',
    inject([PermissionComponent, PermissionService],
      (permissionComponent: PermissionComponent, permissionService: PermissionService) => {
    // given
    let restrictions: Restriction[] = [ <Restriction> { id: 21, action: 'ALL', permission: <Permission> { name: 'RESOURCE' } },
      <Restriction> { id: 22, action: 'ALL', permission: <Permission> { name: 'DEPLOYMENT' } },
      <Restriction> { id: 23, action: 'CREATE', permission: <Permission> { name: 'RESOURCE' } }] ;
    permissionComponent.selectedRoleName = 'TESTER';
    permissionComponent.roleNames = [ 'tester', 'role' ];
    spyOn(permissionService, 'getRoleWithRestrictions').and.returnValue(Observable.of(restrictions));
    // when
    permissionComponent.onChangeRole();
    // then
    expect(permissionService.getRoleWithRestrictions).toHaveBeenCalledWith('TESTER');
    expect(permissionComponent.restrictions.length).toBe(3);
    expect(permissionComponent.restrictions[0].id).toBe(22);
    expect(permissionComponent.restrictions[1].id).toBe(21);
    expect(permissionComponent.restrictions[2].id).toBe(23);
  }));

  it('should not invoke PermissionService on changeRole if selected Role does not exist',
    inject([PermissionComponent, PermissionService],
      (permissionComponent: PermissionComponent, permissionService: PermissionService) => {
        // given
        permissionComponent.restrictions = [ <Restriction> { id: 31 }, <Restriction> { id: 32 } ];
        permissionComponent.selectedRoleName = 'TESTER';
        permissionComponent.roleNames = [ 'role' ];
        spyOn(permissionService, 'getRoleWithRestrictions').and.callThrough();
        // when
        permissionComponent.onChangeRole();
        // then
        expect(permissionService.getRoleWithRestrictions).not.toHaveBeenCalled();
        expect(permissionComponent.restrictions).toEqual([]);
  }));

  it('should invoke PermissionService and sort the Restrictions by Permission.name, action on changeUser if selected User exists',
    inject([PermissionComponent, PermissionService],
      (permissionComponent: PermissionComponent, permissionService: PermissionService) => {
      // given
      let restrictions: Restriction[] = [ <Restriction> { id: 41, action: 'DELETE', permission: <Permission> { name: 'SAME' } },
        <Restriction> { id: 42, action: 'CREATE', permission: <Permission> { name: 'SAME' } } ];
      permissionComponent.selectedUserName = 'Tester';
      permissionComponent.userNames = [ 'tester', 'user' ];
      spyOn(permissionService, 'getUserWithRestrictions').and.returnValue(Observable.of(restrictions));
      // when
      permissionComponent.onChangeUser();
      // then
      expect(permissionService.getUserWithRestrictions).toHaveBeenCalledWith('Tester');
      expect(permissionComponent.restrictions.length).toBe(2);
      expect(permissionComponent.restrictions[0].id).toBe(42);
      expect(permissionComponent.restrictions[1].id).toBe(41);
  }));

  it('should not invoke PermissionService on changeUser if selected User does not exist',
    inject([PermissionComponent, PermissionService],
      (permissionComponent: PermissionComponent, permissionService: PermissionService) => {
        // given
        permissionComponent.restrictions = [ <Restriction> { id: 51 }, <Restriction> { id: 52 } ];
        permissionComponent.selectedUserName = 'Tester';
        permissionComponent.userNames = [ 'user' ];
        spyOn(permissionService, 'getUserWithRestrictions').and.callThrough();
        // when
        permissionComponent.onChangeUser();
        // then
        expect(permissionService.getUserWithRestrictions).not.toHaveBeenCalled();
        expect(permissionComponent.restrictions).toEqual([]);
  }));

  it('should invoke PermissionService on removeRestriction',
    inject([PermissionComponent, PermissionService],
      (permissionComponent: PermissionComponent, permissionService: PermissionService) => {
      // given
      permissionComponent.restrictions = [<Restriction> { id: 121, contextName: 'T' }, <Restriction> { id: 122, contextName: 'B' }];
      permissionComponent.restriction = <Restriction> { id: 122, contextName: 'B' };
      spyOn(permissionService, 'removeRestriction').and.callThrough();
      // when
      permissionComponent.removeRestriction(122);
      // then
      expect(permissionService.removeRestriction).toHaveBeenCalledWith(122);
      expect(permissionComponent.restrictions).toContain({ id: 121, contextName: 'T' });
  }));

  it('should reset the Restriction and error message on cancel',
    inject([PermissionComponent],
      (permissionComponent: PermissionComponent) => {
      // given
      permissionComponent.restriction = <Restriction> { id: 111, contextName: 'T', permission: { name: 'aPermission' } };
      permissionComponent.backupRestriction = <Restriction> { id: 111, contextName: 'B', permission: { name: 'aPermission' } };
      permissionComponent.errorMessage = 'Error';
      // when
      permissionComponent.cancel();
      // then
      expect(permissionComponent.restriction).toBeNull();
      expect(permissionComponent.backupRestriction).toBeNull();
      expect(permissionComponent.errorMessage).toBeNull();
  }));

  it('should create a copy of the original Restriction on modifyRestriction',
    inject([PermissionComponent],
      (permissionComponent: PermissionComponent) => {
      // given
      expect(permissionComponent.restriction).toBeNull();
      expect(permissionComponent.backupRestriction).toBeNull();
      // when
      permissionComponent.modifyRestriction(<Restriction> { id: 123 });
      // then
      expect(permissionComponent.restriction).not.toBeNull();
      expect(permissionComponent.restriction.id).toBe(123);
      expect(permissionComponent.backupRestriction).not.toBeNull();
      expect(permissionComponent.backupRestriction.id).toBe(123);
  }));

  it('should invoke PermissionService.updatePermission on persistRestriction for an existing Restriction',
    inject([PermissionComponent, PermissionService],
      (permissionComponent: PermissionComponent, permissionService: PermissionService) => {
      // given
      permissionComponent.restriction = <Restriction> { id: 111, contextName: 'T' };
      spyOn(permissionService, 'updateRestriction').and.callThrough();
      // when
      permissionComponent.persistRestriction();
      // then
      expect(permissionService.updateRestriction).toHaveBeenCalledWith(permissionComponent.restriction);
  }));

  it('should invoke PermissionService.createPermission on persistRestriction for a new Restriction',
    inject([PermissionComponent, PermissionService],
      (permissionComponent: PermissionComponent, permissionService: PermissionService) => {
      // given
      permissionComponent.restriction = <Restriction> { id: null, contextName: 'S' };
      spyOn(permissionService, 'createRestriction').and.callThrough();
      // when
      permissionComponent.persistRestriction();
      // then
      expect(permissionService.createRestriction).toHaveBeenCalledWith(permissionComponent.restriction, false);
  }));

  it('should initialize an empty Restriction on addRestriction',
    inject([PermissionComponent],
      (permissionComponent: PermissionComponent) => {
      // given
      expect(permissionComponent.restriction).toBeNull();
      // when
      permissionComponent.addRestriction();
      // then
      expect(permissionComponent.restriction).not.toBeNull();
      expect(permissionComponent.restriction.id).toBeNull();
  }));

  describe('PermissionComponent with param restrictionType (type User)', () => {
    beforeEach(() => TestBed.configureTestingModule({
      imports: [
        CommonModule,
        RouterTestingModule.withRoutes([
          {path: 'permission', component: DummyComponent}
        ])
      ],
      providers: [
        BaseRequestOptions, {
          provide: ActivatedRoute,
          useValue: {
            params: Observable.of({restrictionType: 'user'})
          },
        },
        MockBackend,
        {
          provide: Http,
          useFactory: function (backend: ConnectionBackend, defaultOptions: BaseRequestOptions) {
            return new Http(backend, defaultOptions);
          },
          deps: [MockBackend, BaseRequestOptions]
        },
        EnvironmentService,
        PermissionService,
        ResourceService,
        PermissionComponent,
        AppState,
      ],
      declarations: [DummyComponent],
    }));

    it('should invoke some services on ngOnInt',
      inject([PermissionComponent, PermissionService, EnvironmentService, ResourceService],
        (permissionComponent: PermissionComponent, permissionService: PermissionService,
         environmentService: EnvironmentService, resourceService: ResourceService) => {
          // given
          let permissions: Permission[] = [ {name: 'RESOURCE', old: false}, {name: 'RESOURCE_TYPE', old: false}];
          let environments: Environment[] = [ <Environment> { id: 1, name: 'U', parent: 'Dev' },
            <Environment> { id: 2, name: 'V', parent: 'Dev' }, <Environment> { id: 3, name: 'T', parent: 'Test' } ];
          spyOn(permissionService, 'getAllPermissionEnumValues').and.returnValue(Observable.of(permissions));
          spyOn(environmentService, 'getAllIncludingGroups').and.returnValue(Observable.of(environments));
          spyOn(resourceService, 'getAllResourceGroups').and.callThrough();
          spyOn(resourceService, 'getAllResourceTypes').and.callThrough();
          spyOn(permissionService, 'getAllUserRestrictionNames').and.callThrough();
          // when
          permissionComponent.ngOnInit();
          // then
          expect(permissionComponent.delegationMode).toBeFalsy();
          expect(permissionComponent.restrictionType).toEqual('user');
          expect(permissionService.getAllPermissionEnumValues).toHaveBeenCalled();
          expect(environmentService.getAllIncludingGroups).toHaveBeenCalled();
          expect(resourceService.getAllResourceGroups).toHaveBeenCalled();
          expect(resourceService.getAllResourceTypes).toHaveBeenCalled();
          expect(permissionService.getAllUserRestrictionNames).toHaveBeenCalled();
          expect(permissionComponent.permissions).toEqual(permissions);
          expect(permissionComponent.restriction).toBeNull();
          expect(permissionComponent.groupedEnvironments['All']).toContain({ id: null, name: null, parent: 'All', selected: false });
          expect(permissionComponent.groupedEnvironments['Dev']).toContain({ id: 1, name: 'U', parent: 'Dev', selected: false },
            { id: 2, name: 'V', parent: 'Dev', selected: false });
          expect(permissionComponent.groupedEnvironments['Test']).toContain({ id: 3, name: 'T', parent: 'Test', selected: false });
    }));

  });

  describe('PermissionComponent with param actingUser (delegation mode)', () => {
    beforeEach(() => TestBed.configureTestingModule({
      imports: [
        CommonModule,
        RouterTestingModule.withRoutes([
          {path: 'permission', component: DummyComponent}
        ])
      ],
      providers: [
        BaseRequestOptions, {
          provide: ActivatedRoute,
          useValue: {
            params: Observable.of({actingUser: 'testUser'})
          },
        },
        MockBackend,
        {
          provide: Http,
          useFactory: function (backend: ConnectionBackend, defaultOptions: BaseRequestOptions) {
            return new Http(backend, defaultOptions);
          },
          deps: [MockBackend, BaseRequestOptions]
        },
        EnvironmentService,
        PermissionService,
        ResourceService,
        PermissionComponent,
        AppState,
      ],
      declarations: [DummyComponent],
    }));

    it('should invoke some services on ngOnInt',
      inject([PermissionComponent, PermissionService, EnvironmentService, ResourceService],
        (permissionComponent: PermissionComponent, permissionService: PermissionService,
         environmentService: EnvironmentService, resourceService: ResourceService) => {
          // given
          let userNames: string[] = [ 'someUserName', 'anotherUserName', 'testUser' ];
          let restrictions: Restriction[] = [ <Restriction> { id: 1, action: 'CREATE', permission: { name: 'RESOURCE', old: false } },
            <Restriction> { id: 2, action: 'UPDATE', permission: { name: 'RESOURCE_TYPE', old: false } }];
          let permissions: Permission[] = [ {name: 'RESOURCE', old: false}, {name: 'RESOURCE_TYPE', old: false}];
          let environments: Environment[] = [ <Environment> { id: 1, name: 'U', parent: 'Dev' },
            <Environment> { id: 2, name: 'V', parent: 'Dev' }, <Environment> { id: 3, name: 'T', parent: 'Test' } ];
          spyOn(permissionService, 'getAllUserRestrictionNames').and.returnValue(Observable.of(userNames));
          spyOn(permissionService, 'getOwnUserAndRoleRestrictions').and.returnValue(Observable.of(restrictions));
          spyOn(environmentService, 'getAllIncludingGroups').and.returnValue(Observable.of(environments));
          spyOn(resourceService, 'getAllResourceGroups').and.callThrough();
          spyOn(resourceService, 'getAllResourceTypes').and.callThrough();
          // when
          permissionComponent.ngOnInit();
          // then
          expect(permissionComponent.delegationMode).toBeTruthy();
          expect(permissionComponent.restrictionType).toEqual('user');
          expect(permissionComponent.actingUserName).toEqual('testUser');
          expect(permissionService.getAllUserRestrictionNames).toHaveBeenCalled();
          expect(permissionComponent.userNames.length).toEqual(2);
          expect(permissionComponent.userNames).not.toContain('testUser');
          expect(environmentService.getAllIncludingGroups).toHaveBeenCalled();
          expect(resourceService.getAllResourceGroups).toHaveBeenCalled();
          expect(resourceService.getAllResourceTypes).toHaveBeenCalled();
          expect(permissionService.getOwnUserAndRoleRestrictions).toHaveBeenCalled();
          expect(permissionComponent.assignableRestrictions).toEqual(restrictions);
          expect(permissionComponent.assignablePermissions).toEqual(permissions);
          expect(permissionComponent.restriction).toBeNull();
        }));

  });

});
