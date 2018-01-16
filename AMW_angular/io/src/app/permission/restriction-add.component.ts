import { Component, Input, Output, EventEmitter, OnChanges, ChangeDetectorRef, AfterViewChecked } from '@angular/core';
import { Action } from './action';
import { Restriction } from './restriction';
import { RestrictionsCreation } from './restrictions-creation';
import { Permission } from './permission';
import { Environment } from '../deployment/environment';
import { Resource } from '../resource/resource';
import { ResourceType } from '../resource/resource-type';
import * as _ from 'lodash';


@Component({
  selector: 'amw-restriction-add',
  templateUrl: './restriction-add.component.html'
})

export class RestrictionAddComponent implements OnChanges, AfterViewChecked {

  actions: Action[] = [{name: 'ALL'} as Action, {name: 'CREATE'} as Action, {name: 'DELETE'} as Action,
    {name: 'READ'} as Action, {name: 'UPDATE'} as Action];
  resourceTypePermissions: string[] = ['ANY', 'DEFAULT_ONLY', 'NON_DEFAULT_ONLY'];
  resourceGroup: Resource = {} as Resource;

  selectedPermissionNames: string[] = [];
  selectedResourceTypeNames: string[] = [];
  selectedResourceTypePermission: string = null;
  selectedResourceGroupNames: string[] = [];
  selectedContextNames: string[] = [];

  @Input() roleName: string;
  @Input() userNames: any[] = [];
  @Input() permissions: Permission[] = [];
  @Input() groupedEnvironments: { [key: string]: Environment[] } = {};
  @Input() resourceGroups: Resource[] = [];
  @Input() resourceTypes: ResourceType[] = [];
  @Input() availableRestrictions: Restriction[] = [];
  @Input() delegationMode: boolean = false;
  @Output() cancelEdit: EventEmitter<boolean> = new EventEmitter<boolean>();
  @Output() saveRestrictions: EventEmitter<RestrictionsCreation> = new EventEmitter<RestrictionsCreation>();

  similarRestrictions: Restriction[] = [];

  availableEnvironments: string[] = [];

  finalUserNames: string[] = [];

  constructor(private cdRef: ChangeDetectorRef) {
  }

  ngAfterViewChecked() {
    // explicit change detection to avoid "expression-has-changed-after-it-was-checked-error"
    this.cdRef.detectChanges();
  }

  ngOnChanges() {
    if (!this.delegationMode) {
      this.deSelectAllEnvironments();
/*      if (this.restriction.resourceGroupId) {
        this.resourceGroup = {...this.resourceGroups.find((rg) => rg.id === this.restriction.resourceGroupId)};
      } else {
        this.resourceGroup = {} as Resource;
      }*/
    }
  }

  getTitle(): string {
    return 'Add';
  }

  cancel() {
    this.cancelEdit.emit(true);
  }

  persistRestriction() {
    this.userNames.forEach((user) => {
      this.finalUserNames.push(user.label);
    });

    let restrictionsCreation: RestrictionsCreation = { roleName: this.roleName, userNames: this.finalUserNames,
      permissionNames: this.selectedPermissionNames, resourceGroupIds: this.getSelectedGroupIds(),
      resourceTypeNames: this.selectedResourceTypeNames, resourceTypePermission: this.selectedResourceTypePermission,
      contextNames: this.getSelectedEnvNames(), actions: this.getSelectedActionNames() };
    this.saveRestrictions.emit(restrictionsCreation);

    this.deSelectAllActions();
    this.deSelectAllEnvironments();
    this.selectedPermissionNames = [];
    this.selectedResourceTypeNames = [];
    this.selectedResourceTypePermission = null;
    this.selectedResourceGroupNames = [];
    this.selectedContextNames = [];

  }

  isValidForm(): boolean {
    return this.checkType() && this.checkGroup();
  }

  isResourceTypePermissionAssignable(): boolean {
    return this.selectedResourceTypeNames.length < 1 && this.selectedResourceGroupNames.length < 1;
  }

  isResourceTypeAssignable(): boolean {
    return (this.selectedResourceTypePermission === 'ANY' || !this.selectedResourceTypePermission)
      && this.selectedResourceGroupNames.length < 1;
  }

  isResourceGroupAssignable(): boolean {
    return (this.selectedResourceTypePermission === 'ANY' || !this.selectedResourceTypePermission)
      && this.selectedResourceTypeNames.length < 1;
  }

  clearTypeAndGroup() {
    if (this.selectedResourceTypePermission !== 'ANY') {
      this.selectedResourceTypeNames = [];
      this.selectedResourceGroupNames = [];
      //this.resourceGroup = {} as Resource;
    }
  }

  defineAvailableOptions() {
/*    if (this.restriction.permission) {
      this.restriction.permission = {...this.permissions.find((permission) => permission.name === this.restriction.permission.name)};
      if (this.restriction.permission.old) {
        this.selectedActions = 'ALL';
        this.restrictionsCreation.contextNames = [];
        this.selectedResourceGroupIds = [];
        this.selectedResourceTypeNames = [];
        this.selectedResourceTypePermission = 'ANY';
      } else if (this.delegationMode) {
        this.populateSimilarRestrictions();
        this.resetRestrictionForDelegation();
        this.extractAvailableActions();
      }
    }*/
  }

  checkAvailableEnvironments() {
    if (this.delegationMode) {
      this.preSelectEnvironment();
    }
  }

  getAvailableActions(): string[] {
    return this.delegationMode ? this.extractAvailableActions() : this.actions;
  }

  getAvailableResourceGroups(): Resource[] {
    return this.delegationMode ? this.extractAvailableResourceGroups() : this.resourceGroups;
  }

  getAvailableResourceTypePermissions(): string[] {
    return this.delegationMode ? this.extractAvailableResourceTypePermissions() : this.resourceTypePermissions;
  }

  getAvailableResourceTypes(): ResourceType[] {
    return this.delegationMode ? this.extractAvailableResourceTypes() : this.resourceTypes;
  }

  checkUnique(env: Environment) {
    if (this.delegationMode) {
      const index: number = this.groupedEnvironments[env.parent].indexOf(env);
      const isSelected: boolean = this.groupedEnvironments[env.parent][index].selected;
      this.deSelectAllEnvironments();
      this.groupedEnvironments[env.parent][index].selected = isSelected;
      if (isSelected) {
        this.restriction.contextName = this.groupedEnvironments[env.parent][index].name;
      }
    }
  }

  getEnvironmentGroups(): string[] {
    return Object.keys(this.groupedEnvironments);
  }

  private populateSimilarRestrictions() {
    this.similarRestrictions = _.filter(this.availableRestrictions, [ 'permission.name', this.restriction.permission.name ]);
  }

  private extractAvailableActions(): string[] {
    let actions: string[] = [];
    if (this.similarRestrictions.length > 0) {
      this.similarRestrictions.forEach((restriction) => {
        if (restriction.action === 'ALL') {
          actions = this.actions;
        } else if (actions.indexOf(restriction.action) < 0) {
          actions.push(restriction.action);
        }
      });
      actions.sort();
      if (!this.restriction.action) {
        this.restriction.action = actions[0];
        this.preSelectEnvironment();
      }
    }
    return actions;
  }

  private resetRestrictionForDelegation() {
    this.restriction.action = null;
    this.restriction.contextName = null;
    this.restriction.resourceTypeName = null;
    this.restriction.resourceTypePermission = null;
    this.restriction.resourceGroupId = null;
    this.resourceGroup = {} as Resource;
  }

  private extractAvailableResourceGroups(): Resource[] {
    const groups: Resource[] = [];
    let addAll: boolean;
    if (this.similarRestrictions.length > 0) {
      this.similarRestrictions.forEach((restriction) => {
        if (!addAll && restriction.action === this.restriction.action &&
          (restriction.contextName === null || restriction.contextName === this.restriction.contextName
          || restriction.contextName === this.getParentContextName(this.restriction.contextName))) {
          if (restriction.resourceGroupId === null) {
            addAll = true;
          } else if (!_.some(groups, [ 'id', restriction.resourceGroupId ])) {
            groups.push(_.find(this.resourceGroups, [ 'id', restriction.resourceGroupId ]));
          }
        }
      });
    }
    if (addAll) {
      return this.resourceGroups;
    }
    if (groups.length === 1) {
      this.resourceGroup = groups[0];
    }
    return groups;
  }

  private extractAvailableResourceTypePermissions(): string[] {
    const resourceTypePermissions: string[] = [];
    let addAll: boolean;
    if (this.similarRestrictions.length > 0) {
      this.similarRestrictions.forEach((restriction) => {
        if (restriction.action === this.restriction.action && restriction.resourceGroupId === null &&
          (restriction.contextName === null || restriction.contextName === this.restriction.contextName
          || restriction.contextName === this.getParentContextName(this.restriction.contextName))) {
          if (!addAll && resourceTypePermissions.indexOf(restriction.resourceTypePermission) < 0) {
            if (restriction.resourceTypePermission === 'ANY') {
              addAll = true;
            } else {
              resourceTypePermissions.push(restriction.resourceTypePermission);
            }
          }
        }
      });
    }
    if (addAll) {
      return this.resourceTypePermissions;
    }
    return resourceTypePermissions;
  }

  private extractAvailableResourceTypes(): ResourceType[] {
    const resourceTypes: ResourceType[] = [];
    let addAll: boolean;
    if (this.similarRestrictions.length > 0) {
      this.similarRestrictions.forEach((restriction) => {
        if (restriction.action === this.restriction.action && restriction.resourceGroupId === null &&
          (restriction.contextName === null || restriction.contextName === this.restriction.contextName
          || restriction.contextName === this.getParentContextName(this.restriction.contextName))) {
          if (!addAll && restriction.resourceTypeName === null) {
            addAll = true;
          }
          if (!addAll && !_.some(resourceTypes, [ 'name', restriction.resourceTypeName ])) {
            resourceTypes.push(_.find(this.resourceTypes, [ 'name', restriction.resourceTypeName ]));
          }
        }
      });
    }
    if (addAll) {
      return this.resourceTypes;
    }
    return resourceTypes;
  }

  private clearType() {
    if (this.restriction.resourceGroupId) {
      this.restriction.resourceTypeName = null;
    }
  }

  private clearGroup() {
    if (this.restriction.resourceTypeName) {
      this.restriction.resourceGroupId = null;
      this.resourceGroup = {} as Resource;
    }
  }

  private deSelectAllEnvironments() {
    this.getEnvironmentGroups().forEach((group) => {
      this.groupedEnvironments[group].forEach((environment) => environment.selected = false);
    });
  }

  private deSelectAllActions() {
    this.actions.forEach((action) => action.selected = false);
  }

  private getSelectedEnvNames(): string[] {
    let contextNames: string[] = [];
    this.getEnvironmentGroups().forEach((group) => {
      this.groupedEnvironments[group].forEach((environment) => {
        if (environment.selected === true) {
          contextNames.push(environment.name);
        }
      });
    });
    return contextNames;
  }

  private getSelectedActionNames(): string[] {
    let actionNames: string[] = [];
    this.actions.forEach((action) => {
      if (action.selected === true) {
        actionNames.push(action.name);
      }
    });
    return actionNames;
  }

  private getSelectedGroupIds():number[] {
    let groupIds: number[] = [];
    this.selectedResourceGroupNames.forEach((groupName) => {
      groupIds.push(this.resourceGroups.find((rg) => rg.name.toLowerCase() === groupName.toLowerCase()).id);
    });
    return groupIds;
  }

  private disableAllEnvironments() {
    this.getEnvironmentGroups().forEach((group) => {
      this.groupedEnvironments[group].forEach((environment) => environment.disabled = true);
    });
  }

  private enableAllEnvironments() {
    this.getEnvironmentGroups().forEach((group) => {
      this.groupedEnvironments[group].forEach((environment) => environment.disabled = false);
    });
  }

  private enableEnvironment(contextName: string) {
    this.getEnvironmentGroups().forEach((group) => {
      this.groupedEnvironments[group].forEach((environment) => {
        if (environment.name === contextName) {
          environment.disabled = false;
          return;
        }
      });
    });
  }

  private getParentContextName(contextName: string): string {
    const keys = this.getEnvironmentGroups();
    const len: number = keys.length;
    for (let i = 0; i < len; i++) {
      if (_.some(this.groupedEnvironments[keys[i]], ['name', contextName])) {
        return keys[i];
      }
    }
    return null;
  }

}
