import { Component, Input, Output, EventEmitter, OnChanges, ChangeDetectorRef, AfterViewChecked } from '@angular/core';
import { Restriction } from './restriction';
import { Permission } from './permission';
import { Environment } from '../deployment/environment';
import { Resource } from '../resource/resource';
import { ResourceType } from '../resource/resource-type';
import * as _ from 'lodash';

@Component({
  selector: 'amw-restriction',
  templateUrl: './restriction.component.html'
})

export class RestrictionComponent implements OnChanges, AfterViewChecked {

  actions: string[] = [ 'ALL', 'CREATE', 'READ', 'UPDATE', 'DELETE' ];
  resourceTypePermissions: string[] = [ 'ANY', 'DEFAULT_ONLY', 'NON_DEFAULT_ONLY' ];
  resourceGroup: Resource = <Resource> {};

  @Input() restriction: Restriction;
  @Input() permissions: Permission[] = [];
  @Input() groupedEnvironments: { [key: string]: Environment[] } = {};
  @Input() resourceGroups: Resource[] = [];
  @Input() resourceTypes: ResourceType[] = [];
  @Input() availableRestrictions: Restriction[] = [];
  @Input() delegationMode: boolean = false;
  @Output() cancelEdit: EventEmitter<Restriction> = new EventEmitter<Restriction>();
  @Output() saveRestriction: EventEmitter<Restriction> = new EventEmitter<Restriction>();

  similarRestrictions: Restriction[] = [];

  availableEnvironments: string[] = [];

  constructor(private cdRef: ChangeDetectorRef) {
  }

  ngAfterViewChecked() {
    // explicit change detection to avoid "expression-has-changed-after-it-was-checked-error"
    this.cdRef.detectChanges();
  }

  ngOnChanges() {
    if (!this.delegationMode) {
      this.preSelectEnv(this.restriction.contextName);
      if (this.restriction.resourceGroupId) {
        this.resourceGroup = {...this.resourceGroups.find((rg) => rg.id === this.restriction.resourceGroupId)};
      } else {
        this.resourceGroup = <Resource> {};
      }
    }
  }

  getTitle(): string {
    return (this.restriction.id) ? 'Edit' : 'Create';
  }

  cancel() {
    this.cancelEdit.emit(this.restriction);
  }

  persistRestriction() {
    if (!this.restriction.resourceTypeName) {
      this.restriction.resourceTypeName = null;
    }
    this.saveRestriction.emit(this.restriction);
  }

  isValidForm(): boolean {
    return this.checkType() && this.checkGroup();
  }

  isResourceTypeAssignable(): boolean {
    let ty: boolean = (this.restriction.resourceTypePermission === 'ANY' || !this.restriction.resourceTypePermission)
      && !this.restriction.resourceGroupId;
    console.log('isResourceTypeAssignable ' +ty);
    return ty;
  }

  isResourceGroupAssignable(): boolean {
    let gr: boolean = (this.restriction.resourceTypePermission === 'ANY' || !this.restriction.resourceTypePermission)
      && !this.restriction.resourceTypeName;
    console.log('isResourceGroupAssignable ' +gr);
    return gr;
  }

  clearTypeAndGroup() {
    if (this.restriction.resourceTypePermission !== 'ANY') {
      this.restriction.resourceTypeName = null;
      this.restriction.resourceGroupId = null;
      this.resourceGroup = <Resource> {};
    }
  }

  defineAvailableOptions() {
    if (this.restriction.permission) {
      this.restriction.permission = {...this.permissions.find((permission) => permission.name === this.restriction.permission.name)};
      if (this.restriction.permission.old) {
        this.restriction.action = 'ALL';
        this.restriction.contextName = null;
        this.restriction.resourceGroupId = null;
        this.restriction.resourceTypeName = null;
        this.restriction.resourceTypePermission = 'ANY';
      } else if (this.delegationMode) {
        this.populateSimilarRestrictions();
        this.extractAvailableActions();
        this.resetRestrictionForDelegation();
      }
    }
  }

  getAvailableActions(): string[] {
    return this.delegationMode ? this.extractAvailableActions() : this.actions;
  }

  filterAvailableValues() {
    if (this.delegationMode) {
      this.preSelectEnvironment();
      this.resetRestrictionForDelegation();
      // this.extractAvailableResourceTypePermissions();
      // this.extractAvailableResourceTypes();
      // this.extractAvailableResourceGroups();
    }
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

  checkType(): boolean {
    if (this.restriction.resourceTypeName) {
      let valid: boolean = _.find(this.resourceTypes, {name: this.restriction.resourceTypeName}) ? true : false;
      if (valid) {
        this.clearGroup();
      }
      return valid;
    }
    return true;
  }

  checkGroup(): boolean {
    if (this.resourceGroup.name) {
      let selectedResource: Resource = this.resourceGroups.find((rg) => rg.name.toLowerCase() === this.resourceGroup.name.toLowerCase());
      if (!selectedResource) {
        return false;
      }
      this.resourceGroup = {...selectedResource};
      this.restriction.resourceGroupId = this.resourceGroup.id;
      this.clearType();
      return true;
    }
    this.restriction.resourceGroupId = null;
    return true;
  }

  checkUnique(env: Environment) {
    let index: number = this.groupedEnvironments[env.parent].indexOf(env);
    let state: boolean = this.groupedEnvironments[env.parent][index].selected;
    this.deSelectAllEnvironments();
    this.groupedEnvironments[env.parent][index].selected = state;
    if (state) {
      this.restriction.contextName = this.groupedEnvironments[env.parent][index].name;
    }
  }

  getEnvironmentGroups(): string[] {
    return Object.keys(this.groupedEnvironments);
  }

  private populateSimilarRestrictions() {
    this.similarRestrictions = _.filter(this.availableRestrictions, [ 'permission.name', this.restriction.permission.name ]);
  }

  private preSelectEnvironment() {
    this.disableAllEnvironments();
    this.resourceGroup = <Resource> {};
    let isPreSet: boolean;
    this.similarRestrictions.forEach((restriction) => {
      if (restriction.action === 'ALL' || restriction.action === this.restriction.action) {
        this.availableEnvironments.push(restriction.contextName);
        if (!isPreSet) {
          this.preSelectEnv(restriction.contextName);
          isPreSet = true;
        }
        if (!restriction.contextName) {
          // null = All
          this.enableAllEnvironments();
        } else {
          this.enableEnvironment(restriction.contextName);
        }
      }
    });
  }

  private extractAvailableActions(): string[] {
    let actions: string[] = [];
    if (this.similarRestrictions.length > 0) {
      this.similarRestrictions.forEach((restriction) => {
        if (actions.indexOf(restriction.action) < 0) {
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
    console.log('resetRestrictionForDelegation');
    this.restriction.resourceTypeName = null;
    this.restriction.resourceTypePermission = null;
    this.restriction.resourceGroupId = null;
    this.resourceGroup = <Resource> {};
  }

  private extractAvailableResourceGroups(): Resource[] {
    let groups: Resource[] = [];
    if (this.similarRestrictions.length > 0) {
      this.similarRestrictions.forEach((restriction) => {
        if (restriction.action === this.restriction.action) { // && (restriction.resourceTypeName === 'ANY' || restriction.resourceTypeName === null)) {
          if (restriction.resourceGroupId === null) {
            return this.resourceGroups;
          }
          if (!_.some(groups, [ 'id', restriction.resourceGroupId ])) {
            groups.push(_.find(this.resourceGroups, [ 'id', restriction.resourceGroupId ]));
          }
        }
      });
    }
    if (groups.length === 1) {
      this.resourceGroup = groups[0];
    }
    return groups.length > 0 ? groups : this.resourceGroups;
  }

  private extractAvailableResourceTypePermissions(): string[] {
    let resourceTypePermissions: string[] = [];
    if (this.similarRestrictions.length > 0) {
      this.similarRestrictions.forEach((restriction) => {
        if (restriction.action === this.restriction.action && restriction.resourceGroupId === null) {
          if (resourceTypePermissions.indexOf(restriction.resourceTypePermission) < 0) {
            if (restriction.resourceTypePermission === 'ANY') {
              return this.resourceTypePermissions;
            }
            resourceTypePermissions.push(restriction.resourceTypePermission);
          }
        }
      });
    }
    return resourceTypePermissions.length > 0 ? resourceTypePermissions : this.resourceTypePermissions;
  }

  private extractAvailableResourceTypes(): ResourceType[] {
    let resourceTypes: ResourceType[] = [];
    if (this.similarRestrictions.length > 0) {
      this.similarRestrictions.forEach((restriction) => {
        if (restriction.action === this.restriction.action && restriction.resourceGroupId === null) {
          if (restriction.resourceTypeName === null) {
            return this.resourceTypes;
          }
          if (!_.some(resourceTypes, [ 'name', restriction.resourceTypeName ])) {
            resourceTypes.push(_.find(this.resourceTypes, [ 'name', restriction.resourceTypeName ]));
          }
        }
      });
    }
    return resourceTypes.length > 0 ? resourceTypes : this.resourceTypes;
  }

  private clearType() {
    if (this.restriction.resourceGroupId) {
      this.restriction.resourceTypeName = null;
    }
  }

  private clearGroup() {
    if (this.restriction.resourceTypeName) {
      this.restriction.resourceGroupId = null;
      this.resourceGroup = <Resource> {};
    }
  }

  private deSelectAllEnvironments() {
    console.log('deSelectAllEnvironments');
    this.getEnvironmentGroups().forEach((group) => {
      this.groupedEnvironments[group].forEach((environment) => environment.selected = false);
    });
  }

  private preSelectEnv(contextName: string) {
    this.deSelectAllEnvironments();
    this.getEnvironmentGroups().forEach((group) => {
      this.groupedEnvironments[group].forEach((environment) => {
        if (environment.name === contextName) {
          environment.selected = true;
          return;
        }
      });
    });
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

}
