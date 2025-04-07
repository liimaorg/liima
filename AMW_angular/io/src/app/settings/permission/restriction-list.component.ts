import { Component, computed, input, output } from '@angular/core';
import { Restriction } from './restriction';
import * as _ from 'lodash';
import { Resource } from '../../resource/resource';
import { TableComponent, TableColumnType } from '../../shared/table/table.component';

@Component({
  selector: 'app-restriction-list',
  templateUrl: './restriction-list.component.html',
  standalone: true,
  imports: [TableComponent],
})
export class RestrictionListComponent {
  delegationMode = input.required<boolean>();
  restrictions = input.required<Restriction[]>();
  resourceGroups = input.required<Resource[]>();
  deleteRestriction = output<number>();
  editRestriction = output<number>();
  restrictionsTableData = computed(() =>
    this.restrictions().map((res) => {
      return {
        id: res.id,
        permissionName: res.permission.name,
        permissionGlobal: res.permission.old,
        action: res.action,
        contextName: res.contextName,
        resourceGroupName: this.getGroupName(res.resourceGroupId),
        resourceTypeName: res.resourceTypeName,
        resourceTypePermission: res.resourceTypePermission,
      };
    }),
  );

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

  restrictionsHeader(): TableColumnType<{
    id: number;
    permissionName: string;
    permissionGlobal: boolean;
    action: string;
    contextName: string;
    resourceGroupName: number;
    resourceTypeName: string;
    resourceTypePermission: string;
  }>[] {
    return [
      {
        key: 'permissionName',
        columnTitle: 'Permission',
      },
      {
        key: 'permissionGlobal',
        columnTitle: 'Global',
        cellType: 'icon',
        iconMapping: [
          { value: true, icon: 'check' },
          { value: false, icon: null },
        ],
      },
      {
        key: 'action',
        columnTitle: 'Action',
      },
      {
        key: 'contextName',
        columnTitle: 'Environment',
      },
      {
        key: 'resourceGroupName',
        columnTitle: 'Res. Group',
      },
      {
        key: 'resourceTypeName',
        columnTitle: 'Res. Type',
      },
      {
        key: 'resourceTypePermission',
        columnTitle: 'Res. Type Cat.',
      },
    ];
  }
}
