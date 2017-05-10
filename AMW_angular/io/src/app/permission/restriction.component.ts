import { Component, Input, Output, EventEmitter, OnChanges } from '@angular/core';
import { Restriction } from './restriction';
import { Environment } from '../deployment/environment';
import { Resource } from '../resource/resource';
import { ResourceType } from '../resource/resource-type';
import * as _ from 'lodash';

@Component({
  selector: 'amw-restriction',
  templateUrl: './restriction.component.html'
})

export class RestrictionComponent implements OnChanges {

  actions: string[] = [ 'ALL', 'CREATE', 'READ', 'UPDATE', 'DELETE' ];
  resourceTypePermissions: string[] = [ 'ANY', 'DEFAULT_ONLY', 'NON_DEFAULT_ONLY' ];

  @Input() restriction: Restriction;
  @Input() permissionNames: string[] = [];
  @Input() groupedEnvironments: { [key: string]: Environment[] } = {};
  @Input() resourceGroups: Resource[] = [];
  @Input() resourceTypes: ResourceType[] = [];
  @Output() cancelEdit: EventEmitter<Restriction> = new EventEmitter<Restriction>();
  @Output() saveRestriction: EventEmitter<Restriction> = new EventEmitter<Restriction>();

  ngOnChanges() {
    this.deSelectAllEnvironments();
    this.preSelectEnv(this.restriction.contextName);
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

  isValidForm() {
    return this.checkType();
  }

  checkType() {
    if (this.restriction.resourceTypeName) {
      return _.find(this.resourceTypes, {name: this.restriction.resourceTypeName}) ? true : false;
    }
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

  private deSelectAllEnvironments() {
    this.getEnvironmentGroups().forEach((group) => {
      this.groupedEnvironments[group].forEach((environment) => environment.selected = false);
    });
  }

  private preSelectEnv(contextName: string) {
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
