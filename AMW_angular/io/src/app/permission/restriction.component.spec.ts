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
      let emptyEnvironment: Environment[] = [ { id: null, name: null, parent: 'All', selected: false, disabled: false } ];
      let devEnvironments: Environment[] = [ { id: 1, name: 'B', parent: 'Dev', selected: false, disabled: false },
        { id: 2, name: 'C', parent: 'Dev', selected: false, disabled: false } ];
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

  it('should preserve Restriction values on defineAvailableOptions if selected Permission is not old (not global)',
    inject([RestrictionComponent], (restrictionComponent: RestrictionComponent) => {
      // given
      restrictionComponent.restriction = <Restriction> { action: 'CREATE', contextName: 'T', resourceGroupId: 9,
        resourceTypeName: null, resourceTypePermission: 'ANY', permission: <Permission> { name: 'NEO' }};
      restrictionComponent.permissions = [ <Permission> { name: 'NEO', old: false },
        <Permission> { name: 'OLD_GLOBAL', old: true }];
      // when
      restrictionComponent.defineAvailableOptions();
      // then
      expect(restrictionComponent.restriction.action).toBe('CREATE');
      expect(restrictionComponent.restriction.contextName).toBe('T');
      expect(restrictionComponent.restriction.resourceGroupId).toBe(9);
      expect(restrictionComponent.restriction.resourceTypeName).toBeNull();
      expect(restrictionComponent.restriction.resourceTypePermission).toBe('ANY');
  }));

  it('should reset Restriction values on defineAvailableOptions if selected Permission is old (global)',
    inject([RestrictionComponent], (restrictionComponent: RestrictionComponent) => {
      // given
      restrictionComponent.restriction = <Restriction> { action: 'CREATE', contextName: 'T', resourceGroupId: 9,
        resourceTypeName: null, resourceTypePermission: 'ANY', permission: <Permission> { name: 'OLD_GLOBAL' }};
      restrictionComponent.permissions = [ <Permission> { name: 'NEO', old: false },
        <Permission> { name: 'OLD_GLOBAL', old: true }];
      // when
      restrictionComponent.defineAvailableOptions();
      // then
      expect(restrictionComponent.restriction.action).toBe('ALL');
      expect(restrictionComponent.restriction.contextName).toBeNull();
      expect(restrictionComponent.restriction.resourceGroupId).toBeNull();
      expect(restrictionComponent.restriction.resourceTypeName).toBeNull();
      expect(restrictionComponent.restriction.resourceTypePermission).toBe('ANY');
  }));

  it('should prepare available options on defineAvailableOptions if in delegationMode and selected Permission is not old (not global)',
    inject([RestrictionComponent], (restrictionComponent: RestrictionComponent) => {
      // given
      restrictionComponent.delegationMode = true;
      restrictionComponent.restriction = <Restriction> { action: 'CREATE', contextName: 'T', resourceGroupId: 9,
        resourceTypeName: null, resourceTypePermission: 'ANY', permission: <Permission> { name: 'NEO' }};
      restrictionComponent.permissions = [ <Permission> { name: 'NEO', old: false },
        <Permission> { name: 'OLD_GLOBAL', old: true }];
      restrictionComponent.availableRestrictions = [ <Restriction> { action: 'CREATE', contextName: 'T', resourceGroupId: 9,
        resourceTypeName: null, resourceTypePermission: 'ANY', permission: <Permission> { name: 'NEO' }},
        <Restriction> { action: 'UPDATE', contextName: 'S', resourceGroupId: 10,
          resourceTypeName: null, resourceTypePermission: 'ANY', permission: <Permission> { name: 'NEO' }},
        <Restriction> { action: 'UPDATE', contextName: 'T', resourceGroupId: 11,
          resourceTypeName: null, resourceTypePermission: 'ANY', permission: <Permission> { name: 'nada' }} ];
      // when
      restrictionComponent.defineAvailableOptions();
      // then
      expect(restrictionComponent.similarRestrictions.length).toEqual(2);
      expect(restrictionComponent.similarRestrictions[0].permission.name).toBe('NEO');
      expect(restrictionComponent.similarRestrictions[1].permission.name).toBe('NEO');
      expect(restrictionComponent.restriction.contextName).toBeNull();
      expect(restrictionComponent.restriction.resourceGroupId).toBeNull();
      expect(restrictionComponent.restriction.resourceTypeName).toBeNull();
      expect(restrictionComponent.restriction.resourceTypePermission).toBeNull();
  }));

  it('should return filtered resource groups on getAvailableResourceGroups if in delegationMode',
    inject([RestrictionComponent], (restrictionComponent: RestrictionComponent) => {
      // given
      restrictionComponent.delegationMode = true;
      let emptyEnvironment: Environment[] = [ <Environment> { id: null, name: null, parent: 'All'} ];
      let devEnvironments: Environment[] = [ <Environment> { id: 1, name: 'B', parent: 'Dev' },
        <Environment> { id: 2, name: 'C', parent: 'Dev' } ];
      let prodEnvironments: Environment[] = [ <Environment> { id: 12, name: 'P', parent: 'Dev' },
        <Environment> { id: 22, name: 'S', parent: 'Dev' } ];
      restrictionComponent.groupedEnvironments =  { All: emptyEnvironment, Dev: devEnvironments, Pro: prodEnvironments };
      restrictionComponent.resourceGroups = [ <Resource> { id: 1 }, <Resource> { id: 9 }, <Resource> { id: 10 } ];
      // should match parent Dev
      restrictionComponent.restriction = <Restriction> { action: 'CREATE', contextName: 'B', resourceGroupId: 9,
        resourceTypeName: null, resourceTypePermission: 'ALL', permission: <Permission> { name: 'NEO' }};
      restrictionComponent.similarRestrictions = [ <Restriction> { action: 'CREATE', contextName: 'Dev', resourceGroupId: 9,
        resourceTypeName: null, resourceTypePermission: 'ANY', permission: <Permission> { name: 'NEO' }},
        <Restriction> { action: 'CREATE', contextName: 'P', resourceGroupId: 10,
          resourceTypeName: null, resourceTypePermission: 'ANY', permission: <Permission> { name: 'NEO' }} ];
      // when
      let groups = restrictionComponent.getAvailableResourceGroups();
      // then
      expect(groups.length).toEqual(1);
      expect(groups[0].id).toEqual(9);
  }));

  it('should return all resource groups on getAvailableResourceGroups if not in delegationMode',
    inject([RestrictionComponent], (restrictionComponent: RestrictionComponent) => {
      // given
      restrictionComponent.resourceGroups = [ <Resource> { id: 1 }, <Resource> { id: 9 }, <Resource> { id: 10 } ];
      restrictionComponent.restriction = <Restriction> { action: 'CREATE', contextName: 'T', resourceGroupId: 9,
        resourceTypeName: null, resourceTypePermission: 'ALL', permission: <Permission> { name: 'NEO' }};
      // when
      let groups = restrictionComponent.getAvailableResourceGroups();
      // then
      expect(groups.length).toEqual(3);
  }));

  it('should return filtered actions on getAvailableActions if in delegationMode',
    inject([RestrictionComponent], (restrictionComponent: RestrictionComponent) => {
      // given
      restrictionComponent.delegationMode = true;
      restrictionComponent.restriction = <Restriction> { action: 'CREATE', contextName: 'T', resourceGroupId: 9,
        resourceTypeName: null, resourceTypePermission: 'ALL', permission: <Permission> { name: 'NEO' }};
      restrictionComponent.similarRestrictions = [ <Restriction> { action: 'CREATE', contextName: 'T', resourceGroupId: 9,
        resourceTypeName: null, resourceTypePermission: 'ANY', permission: <Permission> { name: 'NEO' }},
        <Restriction> { action: 'READ', contextName: 'S', resourceGroupId: 10,
          resourceTypeName: null, resourceTypePermission: 'ANY', permission: <Permission> { name: 'NEO' }} ];
      // when
      let actions = restrictionComponent.getAvailableActions();
      // then
      expect(actions.length).toEqual(2);
  }));

  it('should return all actions on getAvailableActions if in delegationMode and one of the similar restrictions has action ALL',
    inject([RestrictionComponent], (restrictionComponent: RestrictionComponent) => {
      // given
      restrictionComponent.delegationMode = true;
      restrictionComponent.restriction = <Restriction> { action: 'CREATE', contextName: 'T', resourceGroupId: 9,
        resourceTypeName: null, resourceTypePermission: 'ALL', permission: <Permission> { name: 'NEO' }};
      restrictionComponent.similarRestrictions = [ <Restriction> { action: 'CREATE', contextName: 'T', resourceGroupId: 9,
        resourceTypeName: null, resourceTypePermission: 'ANY', permission: <Permission> { name: 'NEO' }},
        <Restriction> { action: 'ALL', contextName: 'S', resourceGroupId: 10,
          resourceTypeName: null, resourceTypePermission: 'ANY', permission: <Permission> { name: 'NEO' }} ];
      // when
      let actions = restrictionComponent.getAvailableActions();
      // then
      expect(actions.length).toEqual(5);
  }));

  it('should return all actions on getAvailableActions if not in delegationMode',
    inject([RestrictionComponent], (restrictionComponent: RestrictionComponent) => {
      // given // when
      let actions = restrictionComponent.getAvailableActions();
      // then
      expect(actions.length).toEqual(5);
  }));

  it('should return filtered resource types on getAvailableResourceTypes if in delegationMode',
    inject([RestrictionComponent], (restrictionComponent: RestrictionComponent) => {
      // given
      restrictionComponent.delegationMode = true;
      restrictionComponent.resourceTypes = [ { id: 1, name: 'APP' }, { id: 2, name: 'AS' }, { id: 3, name: 'FOO' } ];
      restrictionComponent.restriction = <Restriction> { action: 'CREATE', contextName: 'T', resourceGroupId: null,
        resourceTypeName: 'APP', resourceTypePermission: 'ALL', permission: <Permission> { name: 'NEO' }};
      restrictionComponent.similarRestrictions = [ <Restriction> { action: 'CREATE', contextName: 'T', resourceGroupId: null,
        resourceTypeName: 'FOO', resourceTypePermission: 'ANY', permission: <Permission> { name: 'NEO' }},
        <Restriction> { action: 'CREATE', contextName: 'S', resourceGroupId: null,
          resourceTypeName: 'AS', resourceTypePermission: 'ANY', permission: <Permission> { name: 'NEO' }} ];
      // when
      let types = restrictionComponent.getAvailableResourceTypes();
      // then
      expect(types.length).toEqual(1);
      expect(types[0].name).toBe('FOO');
  }));

  it('should return empty filtered resource types on getAvailableResourceTypes if in delegationMode',
    inject([RestrictionComponent], (restrictionComponent: RestrictionComponent) => {
      // given
      restrictionComponent.delegationMode = true;
      restrictionComponent.resourceTypes = [ { id: 1, name: 'APP' }, { id: 2, name: 'AS' }, { id: 3, name: 'FOO' } ];
      restrictionComponent.restriction = <Restriction> { action: 'CREATE', contextName: 'T', resourceGroupId: null,
        resourceTypeName: 'APP', resourceTypePermission: 'ALL', permission: <Permission> { name: 'NEO' }};
      restrictionComponent.similarRestrictions = [ <Restriction> { action: 'CREATE', contextName: 'X', resourceGroupId: null,
        resourceTypeName: 'FOO', resourceTypePermission: 'ANY', permission: <Permission> { name: 'NEO' }},
        <Restriction> { action: 'CREATE', contextName: 'S', resourceGroupId: null,
          resourceTypeName: 'AS', resourceTypePermission: 'ANY', permission: <Permission> { name: 'NEO' }} ];
      // when
      let types = restrictionComponent.getAvailableResourceTypes();
      // then
      expect(types.length).toEqual(0);
  }));

  it('should return all resource types on getAvailableResourceTypes if in delegationMode and similar restriction has resource type null',
    inject([RestrictionComponent], (restrictionComponent: RestrictionComponent) => {
      // given
      restrictionComponent.delegationMode = true;
      restrictionComponent.resourceTypes = [ { id: 1, name: 'APP' }, { id: 2, name: 'AS' }, { id: 3, name: 'FOO' } ];
      restrictionComponent.restriction = <Restriction> { action: 'CREATE', contextName: 'T', resourceGroupId: null,
        resourceTypeName: 'APP', resourceTypePermission: 'ALL', permission: <Permission> { name: 'NEO' }};
      restrictionComponent.similarRestrictions = [ <Restriction> { action: 'CREATE', contextName: 'T', resourceGroupId: null,
        resourceTypeName: null, resourceTypePermission: 'ANY', permission: <Permission> { name: 'NEO' }},
        <Restriction> { action: 'CREATE', contextName: 'S', resourceGroupId: null,
          resourceTypeName: 'AS', resourceTypePermission: 'ANY', permission: <Permission> { name: 'NEO' }} ];
      // when
      let types = restrictionComponent.getAvailableResourceTypes();
      // then
      expect(types.length).toEqual(3);
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
