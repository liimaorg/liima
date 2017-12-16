import { Component, Input, Output, EventEmitter } from '@angular/core';
import { Restriction } from './restriction';
import { Resource } from '../resource/resource';
import * as _ from 'lodash';

@Component({
  selector: 'amw-restriction-list',
  templateUrl: './restriction-list.component.html'
})

export class RestrictionListComponent {

  @Input() delegationMode: boolean;
  @Input() restrictions: Restriction[] = [];
  @Input() resourceGroups: Resource[] = [];
  @Output() deleteRestriction: EventEmitter<number> = new EventEmitter<number>();
  @Output() editRestriction: EventEmitter<Restriction> = new EventEmitter<Restriction>();

  removeRestriction(id: number) {
    this.deleteRestriction.emit(id);
  }

  modifyRestriction(restriction: Restriction) {
    this.editRestriction.emit(restriction);
  }

  getGroupName(id: number): string {
    if (id) {
      const resource = _.find(this.resourceGroups, {id});
      if (resource) {
        return resource['name'];
      }
    }
    return null;
  }
}
