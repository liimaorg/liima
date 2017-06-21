import {
  Component, Input, Output, EventEmitter, OnChanges, ChangeDetectorRef, AfterContentChecked,
  AfterViewChecked
} from '@angular/core';
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
  @Output() cancelEdit: EventEmitter<Restriction> = new EventEmitter<Restriction>();
  @Output() saveRestriction: EventEmitter<Restriction> = new EventEmitter<Restriction>();

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

  getEnvironmentGroups() {
    return Object.keys(this.groupedEnvironments);
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

  setOld() {
    if (this.restriction.permission) {
      this.restriction.permission = {...this.permissions.find((permission) => permission.name === this.restriction.permission.name)};
      if (this.restriction.permission.old) {
        this.restriction.action = 'ALL';
        this.restriction.contextName = null;
        this.restriction.resourceGroupId = null;
        this.restriction.resourceTypeName = null;
        this.restriction.resourceTypePermission = 'ANY';
      }
    }
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
