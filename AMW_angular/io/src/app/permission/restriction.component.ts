import { Component, Input, Output, EventEmitter } from '@angular/core';
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
  @Input() environments: Environment[] = [];
  @Input() resourceGroups: Resource[] = [];
  @Input() resourceTypes: ResourceType[] = [];
  @Output() cancelEdit: EventEmitter<Restriction> = new EventEmitter<Restriction>();
  @Output() saveRestriction: EventEmitter<Restriction> = new EventEmitter<Restriction>();

  cancel(restriction: Restriction) {
    this.cancelEdit.emit(restriction);
  }

  persistRestriction(restriction: Restriction) {
    this.saveRestriction.emit(restriction);
  }
}
