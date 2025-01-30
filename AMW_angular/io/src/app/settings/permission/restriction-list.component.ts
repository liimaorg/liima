import { Component, computed, input, output } from '@angular/core';
import { Restriction } from './restriction';
import * as _ from 'lodash';
import { Resource } from '../../resource/resource';
import { IconComponent } from '../../shared/icon/icon.component';
import { ButtonComponent } from '../../shared/button/button.component';

@Component({
  selector: 'app-restriction-list',
  templateUrl: './restriction-list.component.html',
  standalone: true,
  imports: [IconComponent, ButtonComponent],
})
export class RestrictionListComponent {
  delegationMode = input.required<boolean>();
  restrictions = input.required<Restriction[]>();
  resourceGroups = input.required<Resource[]>();
  deleteRestriction = output<number>();
  editRestriction = output<number>();

  removeRestriction(id: number) {
    this.deleteRestriction.emit(id);
  }

  modifyRestriction(restrictionId: number) {
    this.editRestriction.emit(restrictionId);
  }

  getGroupName(id: number): string {
    if (id) {
      const resource = _.find(this.resourceGroups(), { id });
      if (resource) {
        return resource['name'];
      }
    }
    return null;
  }
}
