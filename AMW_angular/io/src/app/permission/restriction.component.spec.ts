import { inject, TestBed } from '@angular/core/testing';
import { RestrictionComponent } from './restriction.component';
import { Restriction } from './restriction';
import { Environment } from '../deployment/environment';
import { Resource } from '../resource/resource';
import { Permission } from './permission';
import { ChangeDetectorRef } from '@angular/core';

describe('RestrictionComponent', () => {
  // provide our implementations or mocks to the dependency injector
  beforeEach(() => TestBed.configureTestingModule({
    providers: [
      RestrictionComponent,
      ChangeDetectorRef
    ]
  }));

  it('should preSelect the right Environment on ngOnChanges',
    inject([RestrictionComponent], (restrictionComponent: RestrictionComponent) => {
      // given
      let emptyEnvironment: Environment[] = [ { id: null, name: null, parent: 'All', selected: false } ];
      let devEnvironments: Environment[] = [ { id: 1, name: 'B', parent: 'Dev', selected: false },
        { id: 2, name: 'C', parent: 'Dev', selected: false } ];
      restrictionComponent.groupedEnvironments =  { All: emptyEnvironment, Dev: devEnvironments };
      restrictionComponent.restriction = <Restriction> { contextName: 'C' };
      // when
      restrictionComponent.ngOnChanges();
      // then
      expect(restrictionComponent.groupedEnvironments['All'][0]['selected']).toBeFalsy();
      expect(restrictionComponent.groupedEnvironments['Dev'][0]['selected']).toBeFalsy();
      expect(restrictionComponent.groupedEnvironments['Dev'][1]['selected']).toBeTruthy();
  }));

  it('should return the right title',
    inject([RestrictionComponent], (restrictionComponent: RestrictionComponent) => {
      // given
      restrictionComponent.restriction = <Restriction> { id: 1 };
      // when then
      expect(restrictionComponent.getTitle()).toBe('Edit');
  }));

  it('should return false if ResourceGroup has a name which is not available',
    inject([RestrictionComponent], (restrictionComponent: RestrictionComponent) => {
      // given
      restrictionComponent.resourceGroups = [ <Resource> { id: 21, name: 'Test' } ];
      restrictionComponent.resourceGroup = <Resource> { id: null, name: 'West' };
      // when then
      expect(restrictionComponent.checkGroup()).toBeFalsy();
  }));

  it('should return true if ResourceGroup has a name which is available',
    inject([RestrictionComponent], (restrictionComponent: RestrictionComponent) => {
      // given
      restrictionComponent.resourceGroups = [ <Resource> { id: 21, name: 'Test' }, <Resource> { id: 42, name: 'Rest' } ];
      restrictionComponent.resourceGroup = <Resource> { id: null, name: 'rest' };
      restrictionComponent.restriction = <Restriction> {};
      // when then
      expect(restrictionComponent.checkGroup()).toBeTruthy();
  }));

  it('should return invalid if ResourceType is not available',
    inject([RestrictionComponent], (restrictionComponent: RestrictionComponent) => {
      // given
      restrictionComponent.resourceTypes = [ { id: 1, name: 'APP'}, { id: 2, name: 'APPSERVER' } ];
      restrictionComponent.restriction = <Restriction> { resourceTypeName: 'INVALID' };
      // when then
      expect(restrictionComponent.isValidForm()).toBeFalsy();
  }));

  it('should return valid if ResourceType is available',
    inject([RestrictionComponent], (restrictionComponent: RestrictionComponent) => {
      // given
      restrictionComponent.resourceTypes = [ { id: 1, name: 'APP'}, { id: 2, name: 'APPSERVER' } ];
      restrictionComponent.restriction = <Restriction> { resourceTypeName: 'APPSERVER' };
      // when then
      expect(restrictionComponent.isValidForm()).toBeTruthy();
  }));

  it('should set ResourceTypeName to null if its value is empty',
    inject([RestrictionComponent], (restrictionComponent: RestrictionComponent) => {
      // given
      restrictionComponent.restriction = <Restriction> { resourceTypeName: '' };
      // when
      restrictionComponent.persistRestriction();
      // then
      expect(restrictionComponent.restriction.resourceTypeName).toBeNull();
  }));

  it('should preserve Restriction values if Permission is not old',
    inject([RestrictionComponent], (restrictionComponent: RestrictionComponent) => {
      // given
      restrictionComponent.restriction = <Restriction> { action: 'CREATE', contextName: 'T', resourceGroupId: 9,
        resourceTypeName: null, resourceTypePermission: 'ANY', permission: <Permission> { name: 'neo' }};
      restrictionComponent.permissions = [ <Permission> { name: 'neo', old: false },
        <Permission> { name: 'oldie', old: true }];
      // when
      restrictionComponent.defineAvailableOptions();
      // then
      expect(restrictionComponent.restriction.action).toBe('CREATE');
      expect(restrictionComponent.restriction.contextName).toBe('T');
      expect(restrictionComponent.restriction.resourceGroupId).toBe(9);
      expect(restrictionComponent.restriction.resourceTypeName).toBeNull();
      expect(restrictionComponent.restriction.resourceTypePermission).toBe('ANY');
  }));

  it('should reset Restriction values if Permission is old',
    inject([RestrictionComponent], (restrictionComponent: RestrictionComponent) => {
      // given
      restrictionComponent.restriction = <Restriction> { action: 'CREATE', contextName: 'T', resourceGroupId: 9,
        resourceTypeName: null, resourceTypePermission: 'ANY', permission: <Permission> { name: 'oldie' }};
      restrictionComponent.permissions = [ <Permission> { name: 'neo', old: false },
        <Permission> { name: 'oldie', old: true }];
      // when
      restrictionComponent.defineAvailableOptions();
      // then
      expect(restrictionComponent.restriction.action).toBe('ALL');
      expect(restrictionComponent.restriction.contextName).toBeNull();
      expect(restrictionComponent.restriction.resourceGroupId).toBeNull();
      expect(restrictionComponent.restriction.resourceTypeName).toBeNull();
      expect(restrictionComponent.restriction.resourceTypePermission).toBe('ANY');
  }));

  it('should allow to assign ResourceType if ResourceTypePermission is ANY and ResourceGroup is null',
    inject([RestrictionComponent], (restrictionComponent: RestrictionComponent) => {
      // given
      restrictionComponent.restriction = <Restriction> { action: 'CREATE', contextName: 'T', resourceGroupId: null,
        resourceTypeName: null, resourceTypePermission: 'ANY', permission: <Permission> { name: 'test' }};
      // when
      let possible: boolean = restrictionComponent.isResourceTypeAssignable();
      // then
      expect(possible).toBeTruthy();
  }));

  it('should not allow to assign ResourceType if ResourceGroup is not null',
    inject([RestrictionComponent], (restrictionComponent: RestrictionComponent) => {
      // given
      restrictionComponent.restriction = <Restriction> { action: 'CREATE', contextName: 'T', resourceGroupId: 8,
        resourceTypeName: null, resourceTypePermission: 'ANY', permission: <Permission> { name: 'test' }};
      // when
      let possible: boolean = restrictionComponent.isResourceTypeAssignable();
      // then
      expect(possible).toBeFalsy();
  }));

  it('should not allow to assign ResourceType if ResourceTypePermission is not ANY',
    inject([RestrictionComponent], (restrictionComponent: RestrictionComponent) => {
      // given
      restrictionComponent.restriction = <Restriction> { action: 'CREATE', contextName: 'T', resourceGroupId: null,
        resourceTypeName: null, resourceTypePermission: 'DEFAULT_ONLY', permission: <Permission> { name: 'test' }};
      // when
      let possible: boolean = restrictionComponent.isResourceTypeAssignable();
      // then
      expect(possible).toBeFalsy();
  }));

  it('should allow to assign ResourceGroup if ResourceTypePermission is ANY and ResourceTypeName is null',
    inject([RestrictionComponent], (restrictionComponent: RestrictionComponent) => {
      // given
      restrictionComponent.restriction = <Restriction> { action: 'CREATE', contextName: 'T', resourceGroupId: null,
        resourceTypeName: null, resourceTypePermission: 'ANY', permission: <Permission> { name: 'test' }};
      // when
      let possible: boolean = restrictionComponent.isResourceGroupAssignable();
      // then
      expect(possible).toBeTruthy();
  }));

  it('should not allow to assign ResourceGroup if ResourceTypeName is not null',
    inject([RestrictionComponent], (restrictionComponent: RestrictionComponent) => {
      // given
      restrictionComponent.restriction = <Restriction> { action: 'CREATE', contextName: 'T', resourceGroupId: null,
        resourceTypeName: 'test', resourceTypePermission: 'ANY', permission: <Permission> { name: 'test' }};
      // when
      let possible: boolean = restrictionComponent.isResourceGroupAssignable();
      // then
      expect(possible).toBeFalsy();
  }));

  it('should not allow to assign ResourceGroup if ResourceTypePermission is not ANY',
    inject([RestrictionComponent], (restrictionComponent: RestrictionComponent) => {
      // given
      restrictionComponent.restriction = <Restriction> { action: 'CREATE', contextName: 'T', resourceGroupId: null,
        resourceTypeName: null, resourceTypePermission: 'DEFAULT_ONLY', permission: <Permission> { name: 'test' }};
      // when
      let possible: boolean = restrictionComponent.isResourceGroupAssignable();
      // then
      expect(possible).toBeFalsy();
  }));

});
