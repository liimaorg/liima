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
import { Tag } from './tag';

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
        useFactory(backend: ConnectionBackend, defaultOptions: BaseRequestOptions) {
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
    const permissions: Permission[] = [{name: 'RESOURCE', old: false} as Permission, {name: 'RESOURCE_TYPE', old: false} as Permission];
    const environments: Environment[] = [{id: 1, name: 'U', parent: 'Dev'} as Environment,
      {id: 2, name: 'V', parent: 'Dev'} as Environment, {id: 3, name: 'T', parent: 'Test'} as Environment];
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
    expect(permissionComponent.groupedEnvironments['All']).toContain({id: null, name: null, parent: 'All', selected: false});
    expect(permissionComponent.groupedEnvironments['Dev']).toContain({id: 1, name: 'U', parent: 'Dev', selected: false},
      {id: 2, name: 'V', parent: 'Dev', selected: false});
    expect(permissionComponent.groupedEnvironments['Test']).toContain({id: 3, name: 'T', parent: 'Test', selected: false});
  }));

  it('should invoke PermissionService and sort the Restrictions by Permission.name, action on changeRole if selected Role exists',
    inject([PermissionComponent, PermissionService],
      (permissionComponent: PermissionComponent, permissionService: PermissionService) => {
    // given
    const restrictions: Restriction[] = [{id: 21, action: 'ALL', permission: {name: 'RESOURCE'} as Permission} as Restriction,
      {id: 22, action: 'ALL', permission: {name: 'DEPLOYMENT'} as Permission} as Restriction,
      {id: 23, action: 'CREATE', permission: {name: 'RESOURCE'} as Permission} as Restriction] ;
    permissionComponent.selectedRoleName = 'TESTER';
    permissionComponent.roleNames = ['tester', 'role'];
    spyOn(permissionService, 'getRoleWithRestrictions').and.returnValue(Observable.of(restrictions));
    // when
    permissionComponent.onChangeRole();
    // then
    expect(permissionService.getRoleWithRestrictions).toHaveBeenCalledWith('TESTER');
    expect(permissionComponent.assignedRestrictions.length).toBe(3);
    expect(permissionComponent.assignedRestrictions[0].id).toBe(22);
    expect(permissionComponent.assignedRestrictions[1].id).toBe(21);
    expect(permissionComponent.assignedRestrictions[2].id).toBe(23);
  }));

  it('should trim selectedRoleName on changeRole',
    inject([PermissionComponent],
      (permissionComponent: PermissionComponent) => {
        // given
        permissionComponent.selectedRoleName = ' TESTER ';
        // when
        permissionComponent.onChangeRole();
        // then
        expect(permissionComponent.selectedRoleName).toBe('TESTER');
  }));

  it('should not invoke PermissionService on changeRole if selected Role does not exist',
    inject([PermissionComponent, PermissionService],
      (permissionComponent: PermissionComponent, permissionService: PermissionService) => {
        // given
        permissionComponent.assignedRestrictions = [{id: 31} as Restriction, {id: 32} as Restriction];
        permissionComponent.selectedRoleName = 'TESTER';
        permissionComponent.roleNames = ['role'];
        spyOn(permissionService, 'getRoleWithRestrictions').and.callThrough();
        // when
        permissionComponent.onChangeRole();
        // then
        expect(permissionService.getRoleWithRestrictions).not.toHaveBeenCalled();
        expect(permissionComponent.assignedRestrictions).toEqual([]);
  }));

  it('should convert and trim users (provided as string or Tag) to selectedUserNames on changeUser',
    inject([PermissionComponent],
      (permissionComponent: PermissionComponent) => {
        // given // when
        permissionComponent.onChangeUser(['stringToBeTrimmed ', ' stringToBeTrimmedToo', {label: ' tagToBeTrimmed '} as Tag]);
        // then
        expect(permissionComponent.selectedUserNames[0]).toBe('stringToBeTrimmed');
        expect(permissionComponent.selectedUserNames[1]).toBe('stringToBeTrimmedToo');
        expect(permissionComponent.selectedUserNames[2]).toBe('tagToBeTrimmed');
  }));

  it('should invoke PermissionService and sort the Restrictions by Permission.name, action on changeUser if selected User exists',
    inject([PermissionComponent, PermissionService],
      (permissionComponent: PermissionComponent, permissionService: PermissionService) => {
      // given
      const restrictions: Restriction[] = [{id: 41, action: 'DELETE', permission: {name: 'SAME'} as Permission} as Restriction,
        {id: 42, action: 'CREATE', permission: {name: 'SAME'} as Permission} as Restriction];
      permissionComponent.selectedUserNames = ['Tester'];
      permissionComponent.userNames = ['tester', 'user'];
      spyOn(permissionService, 'getUserWithRestrictions').and.returnValue(Observable.of(restrictions));
      // when
      permissionComponent.onChangeUser(['Tester']);
      // then
      expect(permissionService.getUserWithRestrictions).toHaveBeenCalledWith('Tester');
      expect(permissionComponent.assignedRestrictions.length).toBe(2);
      expect(permissionComponent.assignedRestrictions[0].id).toBe(42);
      expect(permissionComponent.assignedRestrictions[1].id).toBe(41);
  }));

  it('should invoke PermissionService and sort the Restrictions by Permission.name, action on changeUser if selected User (provided as Tag) exists',
    inject([PermissionComponent, PermissionService],
      (permissionComponent: PermissionComponent, permissionService: PermissionService) => {
      // given
      const restrictions: Restriction[] = [{id: 41, action: 'DELETE', permission: {name: 'SAME'} as Permission} as Restriction,
        {id: 42, action: 'CREATE', permission: {name: 'SAME'} as Permission} as Restriction];
      permissionComponent.selectedUserNames = ['Tester'];
      permissionComponent.userNames = ['tester', 'user'];
      spyOn(permissionService, 'getUserWithRestrictions').and.returnValue(Observable.of(restrictions));
      // when
      permissionComponent.onChangeUser([{label: 'Tester'} as Tag]);
      // then
      expect(permissionService.getUserWithRestrictions).toHaveBeenCalledWith('Tester');
      expect(permissionComponent.assignedRestrictions.length).toBe(2);
      expect(permissionComponent.assignedRestrictions[0].id).toBe(42);
      expect(permissionComponent.assignedRestrictions[1].id).toBe(41);
  }));

  it('should not invoke PermissionService on changeUser if selected User does not exist',
    inject([PermissionComponent, PermissionService],
      (permissionComponent: PermissionComponent, permissionService: PermissionService) => {
        // given
        permissionComponent.assignedRestrictions = [{id: 51} as Restriction, {id: 52} as Restriction];
        permissionComponent.selectedUserNames = ['Tester'];
        permissionComponent.userNames = ['user'];
        spyOn(permissionService, 'getUserWithRestrictions').and.callThrough();
        // when
        permissionComponent.onChangeUser(['tester']);
        // then
        expect(permissionService.getUserWithRestrictions).not.toHaveBeenCalled();
        expect(permissionComponent.assignedRestrictions).toEqual([]);
  }));

  it('should not invoke PermissionService on changeUser if selected User (provided as Tag) does not exist',
    inject([PermissionComponent, PermissionService],
    (permissionComponent: PermissionComponent, permissionService: PermissionService) => {
      // given
      permissionComponent.assignedRestrictions = [{id: 51} as Restriction, {id: 52} as Restriction];
      permissionComponent.selectedUserNames = ['Tester'];
      permissionComponent.userNames = ['user'];
      spyOn(permissionService, 'getUserWithRestrictions').and.callThrough();
      // when
      permissionComponent.onChangeUser([{label: 'Tester'} as Tag]);
      // then
      expect(permissionService.getUserWithRestrictions).not.toHaveBeenCalled();
      expect(permissionComponent.assignedRestrictions).toEqual([]);
   }));

  it('should invoke PermissionService on removeRestriction',
    inject([PermissionComponent, PermissionService],
      (permissionComponent: PermissionComponent, permissionService: PermissionService) => {
      // given
      permissionComponent.assignedRestrictions = [{ id: 121, contextName: 'T'} as Restriction, {id: 122, contextName: 'B'} as Restriction];
      permissionComponent.restriction = {id: 122, contextName: 'B'} as Restriction;
      spyOn(permissionService, 'removeRestriction').and.callThrough();
      // when
      permissionComponent.removeRestriction(122);
      // then
      expect(permissionService.removeRestriction).toHaveBeenCalledWith(122);
      expect(permissionComponent.assignedRestrictions).toContain({id: 121, contextName: 'T'});
  }));

  it('should reset the Restriction and error message on cancel',
    inject([PermissionComponent],
      (permissionComponent: PermissionComponent) => {
      // given
      permissionComponent.restriction = {id: 111, contextName: 'T', permission: {name: 'aPermission'}} as Restriction;
      permissionComponent.backupRestriction = {id: 111, contextName: 'B', permission: {name: 'aPermission'}} as Restriction;
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
      permissionComponent.modifyRestriction({id: 123} as Restriction);
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
      permissionComponent.restriction = {id: 111, contextName: 'T'} as Restriction;
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
      permissionComponent.restriction = {id: null, contextName: 'S'} as Restriction;
      spyOn(permissionService, 'createRestriction').and.callThrough();
      // when
      permissionComponent.persistRestriction();
      // then
      expect(permissionService.createRestriction).toHaveBeenCalledWith(permissionComponent.restriction, false);
  }));

  it('should not set create to true and not initialize a Restriction on addRestriction if no User or Role is selected',
    inject([PermissionComponent],
      (permissionComponent: PermissionComponent) => {
      // given
      expect(permissionComponent.create).toBeFalsy();
      expect(permissionComponent.restriction).toBeNull();
      // when
      permissionComponent.addRestriction();
      // then
      expect(permissionComponent.create).toBeFalsy();
      expect(permissionComponent.restriction).toBeNull();
  }));

  it('should set create to true but not initialize a Restriction on addRestriction if a RoleName is selected',
    inject([PermissionComponent],
      (permissionComponent: PermissionComponent) => {
        // given
        permissionComponent.selectedRoleName = 'Test';
        expect(permissionComponent.create).toBeFalsy();
        expect(permissionComponent.restriction).toBeNull();
        // when
        permissionComponent.addRestriction();
        // then
        expect(permissionComponent.create).toBeTruthy();
        expect(permissionComponent.restriction).toBeNull();
  }));

  it('should set create to true but not initialize a Restriction on addRestriction if a UserName is selected',
    inject([PermissionComponent],
      (permissionComponent: PermissionComponent) => {
        // given
        permissionComponent.selectedUserNames = ['Tester'];
        expect(permissionComponent.create).toBeFalsy();
        expect(permissionComponent.restriction).toBeNull();
        // when
        permissionComponent.addRestriction();
        // then
        expect(permissionComponent.create).toBeTruthy();
        expect(permissionComponent.restriction).toBeNull();
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
          useFactory(backend: ConnectionBackend, defaultOptions: BaseRequestOptions) {
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
          const permissions: Permission[] = [{name: 'RESOURCE', old: false} as Permission, {name: 'RESOURCE_TYPE', old: false} as Permission];
          const environments: Environment[] = [{id: 1, name: 'U', parent: 'Dev'} as Environment,
            {id: 2, name: 'V', parent: 'Dev'} as Environment, {id: 3, name: 'T', parent: 'Test'} as Environment];
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
          expect(permissionComponent.groupedEnvironments['All']).toContain({id: null, name: null, parent: 'All', selected: false});
          expect(permissionComponent.groupedEnvironments['Dev']).toContain({id: 1, name: 'U', parent: 'Dev', selected: false},
            {id: 2, name: 'V', parent: 'Dev', selected: false});
          expect(permissionComponent.groupedEnvironments['Test']).toContain({id: 3, name: 'T', parent: 'Test', selected: false});
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
          useFactory(backend: ConnectionBackend, defaultOptions: BaseRequestOptions) {
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
          const userNames: string[] = [ 'someUserName', 'anotherUserName', 'testUser' ];
          const restrictions: Restriction[] = [{id: 1, action: 'CREATE', permission: {name: 'RESOURCE', old: false}} as Restriction,
            {id: 2, action: 'UPDATE', permission: {name: 'RESOURCE_TYPE', old: false}} as Restriction];
          const permissions: Permission[] = [{name: 'RESOURCE', old: false, longName: 'RESOURCE'},
            {name: 'RESOURCE_TYPE', old: false, longName: 'RESOURCE_TYPE'}];
          const environments: Environment[] = [{id: 1, name: 'U', parent: 'Dev'} as Environment,
            {id: 2, name: 'V', parent: 'Dev'} as Environment, {id: 3, name: 'T', parent: 'Test'} as Environment];
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
