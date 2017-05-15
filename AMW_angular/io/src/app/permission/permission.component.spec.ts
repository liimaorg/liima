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
import { Environment } from './environment';
import { AppState } from '../app.service';
import { Restriction } from './restriction';

@Component({
  template: ''
})
class DummyComponent {
}

describe('PermissionComponent', () => {
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
    expect(permissionComponent.permissionNames).toEqual([]);
    expect(permissionComponent.environments).toEqual([ { id: null, name: null, parent: 'All', selected: false } ]);
    expect(permissionComponent.resourceGroups).toEqual([ { id: null, name: null, type: null, version: null, release: null, releases: [] } ]);
    expect(permissionComponent.resourceTypes).toEqual([ { id: null, name: null } ]);
    expect(permissionComponent.restrictionType).toEqual('role');
  }));

  it('should invoke some services on ngOnInt',
    inject([PermissionComponent, PermissionService, EnvironmentService, ResourceService],
      (permissionComponent: PermissionComponent, permissionService: PermissionService, environmentService: EnvironmentService, resourceService: ResourceService) => {
    // given
    spyOn(permissionService, 'getAllPermissionNames').and.callThrough();
    spyOn(environmentService, 'getAll').and.callThrough();
    spyOn(resourceService, 'getAllResourceGroups').and.callThrough();
    spyOn(resourceService, 'getAllResourceTypes').and.callThrough();
    // when
    permissionComponent.ngOnInit();
    // then
    expect(permissionService.getAllPermissionNames).toHaveBeenCalled();
    expect(environmentService.getAll).toHaveBeenCalled();
    expect(resourceService.getAllResourceGroups).toHaveBeenCalled();
    expect(resourceService.getAllResourceTypes).toHaveBeenCalled();
    expect(permissionComponent.restriction).toBeNull();
  }));

  it('should invoke PermissionService on changeRole',
    inject([PermissionComponent, PermissionService],
      (permissionComponent: PermissionComponent, permissionService: PermissionService) => {
    // given
    permissionComponent.selectedRoleName = 'TESTER';
    spyOn(permissionService, 'getRoleWithRestrictions').and.callThrough();
    // when
    permissionComponent.onChangeRole();
    // then
    expect(permissionService.getRoleWithRestrictions).toHaveBeenCalledWith('TESTER')
  }));

  it('should invoke PermissionService on changeUser',
    inject([PermissionComponent, PermissionService],
      (permissionComponent: PermissionComponent, permissionService: PermissionService) => {
      // given
      permissionComponent.selectedUserName = 'tester';
      spyOn(permissionService, 'getUserWithRestrictions').and.callThrough();
      // when
      permissionComponent.onChangeUser();
      // then
      expect(permissionService.getUserWithRestrictions).toHaveBeenCalledWith('tester');
  }));

  it('should invoke PermissionService on removeRestriction',
    inject([PermissionComponent, PermissionService],
      (permissionComponent: PermissionComponent, permissionService: PermissionService) => {
      // given
      spyOn(permissionService, 'removeRestriction').and.callThrough();
      // when
      permissionComponent.removeRestriction(1);
      // then
      expect(permissionService.removeRestriction).toHaveBeenCalledWith(1);
  }));

  it('should reset the Restriction on cancel',
    inject([PermissionComponent],
      (permissionComponent: PermissionComponent) => {
      // given
      permissionComponent.restriction = <Restriction> { id: 111, contextName: 'T' };
      permissionComponent.backupRestriction = <Restriction> { id: 111, contextName: 'B' };
      // when
      permissionComponent.cancel();
      // then
      expect(permissionComponent.restriction).toBeNull();
      expect(permissionComponent.backupRestriction).toBeNull();
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
      expect(permissionService.createRestriction).toHaveBeenCalledWith(permissionComponent.restriction);
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

});
