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

  availableEnvironmentsPerAction: { [key: string]: string[] } = {}

  availableEnvironments: string[] = [];

  constructor(private cdRef: ChangeDetectorRef) {
  }

  ngAfterViewChecked() {
    // explicit change detection to avoid "expression-has-changed-after-it-was-checked-error"
    this.cdRef.detectChanges();
  }

  ngOnChanges() {
    this.preSelectEnv(this.restriction.contextName);
    if (this.restriction.resourceGroupId) {
      this.resourceGroup = {...this.resourceGroups.find((rg) => rg.id === this.restriction.resourceGroupId)};
    } else {
      this.resourceGroup = <Resource> {};
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
    return (this.restriction.resourceTypePermission === 'ANY' || !this.restriction.resourceTypePermission)
      && !this.restriction.resourceGroupId;
  }

  isResourceGroupAssignable(): boolean {
    return (this.restriction.resourceTypePermission === 'ANY' || !this.restriction.resourceTypePermission)
      && !this.restriction.resourceTypeName;
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
      }
    }
  }

  getAvailableActions(): string[] {
    return this.delegationMode ? this.extractAvailableActions() : this.actions;
  }

  populateAvailableEnvironments() {
    if (this.delegationMode) {
      // TODO: fill this.availableEnvironments
      // match this.similarRestrictions.action with this.restriction.action OR ALL
      //this.availableEnvironmentsPerAction;
      this.similarRestrictions.forEach((restriction) => {
        if (restriction.action === 'ALL' || restriction.action === this.restriction.action) {
          this.availableEnvironments.push(restriction.contextName);
        }
      });
    }
  }

  isAvailableEnvironment(contextName: string): boolean {
    return this.delegationMode ? this.checkEnvironment(contextName) : true;
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

  private extractAvailableActions(): string[] {
    let actions: string[] = [];
    //this.availableEnvironmentsPerAction = {};
    this.availableEnvironments = [];
    if (this.similarRestrictions.length > 0) {
      this.similarRestrictions.forEach((restriction) => {
        if (!this.availableEnvironmentsPerAction[restriction.action]) {
          this.availableEnvironmentsPerAction[restriction.action] = [];
        }
        this.availableEnvironmentsPerAction[restriction.action].push(restriction.contextName);
        if (actions.indexOf(restriction.action) < 0) {
          actions.push(restriction.action);
        }
      });
      actions.sort();
      if (!this.restriction.action) {
        this.restriction.action = actions[0];
      }
    }
    return actions;
  }

  private checkEnvironment(contextName: string): boolean {
    if (!contextName) {
      contextName = 'ALL';
    }
    if (this.availableEnvironmentsPerAction[this.restriction.action]) {
      let test: string[] = [];
      test.push(this.availableEnvironmentsPerAction[this.restriction.action]);
      let foo: boolean = _.some(test, 'ALL');
      let bar: boolean = _.some(test, contextName);
      console.log('foo: ' + foo + ' bar: ' +bar + ' col: ' + this.availableEnvironmentsPerAction[this.restriction.action]);
      if (_.some(this.availableEnvironmentsPerAction[this.restriction.action], 'ALL') || _.some(this.availableEnvironmentsPerAction[this.restriction.action], contextName)) {
        return true;
      }
    }
    return false;
    //
    // if (_.some(this.similarRestrictions, { contextName: null, action: 'ALL' } ) ||
    //   _.some(this.similarRestrictions, { contextName: null, action: this.restriction.action } )) {
    //   return true;
    // } else {
    //   for (let i = 0; i < this.similarRestrictions.length; i++) {
    //     if (this.similarRestrictions[i]['contextName'] === contextName &&
    //       (this.similarRestrictions[i]['action'] === 'ALL' || this.similarRestrictions[i]['action'] === this.restriction.action)) {
    //       return true;
    //     }
    //   }
    // }
    // return false;
  }

  private extractAvailableResourceGroups(): Resource[] {
    let groups: Resource[] = [];
    if (this.similarRestrictions.length > 0) {
      if (_.some(this.similarRestrictions, [ 'resourceGroupId', null ])) {
        return this.resourceGroups;
      } else {
        this.similarRestrictions.forEach((restriction) => {
          if (!_.some(groups, [ 'id', restriction.resourceGroupId ])) {
            groups.push(_.find(this.resourceGroups, [ 'id', restriction.resourceGroupId ]));
          }
        });
      }
    }
    return groups;
  }

  private extractAvailableResourceTypePermissions(): string[] {
    let resourceTypePermissions: string[] = [];
    if (this.similarRestrictions.length > 0) {
      this.similarRestrictions.forEach((restriction) => {
        if (resourceTypePermissions.indexOf(restriction.resourceTypePermission) < 0) {
          resourceTypePermissions.push(restriction.resourceTypePermission);
        }
      });
    }
    return resourceTypePermissions;
  }

  private extractAvailableResourceTypes(): ResourceType[] {
    let resourceTypes: ResourceType[] = [];
    if (this.similarRestrictions.length > 0) {
      if (_.some(this.similarRestrictions, [ 'resourceTypeName', null ])) {
        return this.resourceTypes;
      } else {
        this.similarRestrictions.forEach((restriction) => {
          if (!_.some(resourceTypes, [ 'name', restriction.resourceTypeName ])) {
            resourceTypes.push(_.find(this.resourceTypes, [ 'name', restriction.resourceTypeName ]));
          }
        });
      }
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
      this.resourceGroup = <Resource> {};
    }
  }

  private deSelectAllEnvironments() {
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

}
