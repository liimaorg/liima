import { Component, Input, Output, EventEmitter, OnChanges } from '@angular/core';
import { Restriction } from './restriction';
import { Environment } from '../deployment/environment';
import { Resource } from '../resource/resource';
import { ResourceType } from '../resource/resource-type';

@Component({
  selector: 'amw-restriction',
  templateUrl: './restriction.component.html'
})

export class RestrictionComponent {

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
    this.saveRestriction.emit(this.restriction);
  }

  getEnvironmentGroups() {
    return Object.keys(this.groupedEnvironments);
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
    let groups: string[] = this.getEnvironmentGroups();
    for (let i = 0; i < groups.length; i++) {
      for (let j = 0; j < this.groupedEnvironments[groups[i]].length; j++) {
        this.groupedEnvironments[groups[i]][j].selected = false;
      }
    }
  }

  private preSelectEnv(contextName: string) {
    console.log('PreSelecting');
    let groups: string[] = this.getEnvironmentGroups();
    for (let i = 0; i < groups.length; i++) {
      for (let j = 0; j < this.groupedEnvironments[groups[i]].length; j++) {
        if (this.groupedEnvironments[groups[i]][j].name === contextName) {
          this.groupedEnvironments[groups[i]][j].selected = true;
          return;
        }
      }
    }
  }
}
