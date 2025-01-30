import { Component, computed, input, output } from '@angular/core';
import { Restriction } from './restriction';
import * as _ from 'lodash';
import { Resource } from '../../resource/resource';
import { TableComponent, TableHeader } from '../../shared/table/table.component';

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
        roleName: res.roleName,
        userName: res.userName,
        permissionName: res.permission.name,
        permissionGlobal: res.permission.old,
        resourceGroupId: res.resourceGroupId,
        resourceTypeName: res.resourceTypeName,
        resourceTypePermission: res.resourceTypePermission,
        contextName: res.contextName,
        action: res.action,
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

  restrictionsHeader(): TableHeader<{
    id: number;
    roleName: string;
    userName: string;
    permissionName: string;
    permissionGlobal: boolean;
    resourceGroupId: number;
    resourceTypeName: string;
    resourceTypePermission: string;
    contextName: string;
    action: string;
  }>[] {
    return [
      {
        key: 'permissionName',
        title: 'Permission',
      },
      {
        key: 'permissionGlobal',
        title: 'Global',
        type: 'icon',
        iconMapping: [
          { value: true, icon: 'check' },
          { value: false, icon: null },
        ],
      },
      {
        key: 'action',
        title: 'Action',
      },
      {
        key: 'contextName',
        title: 'Environment',
      },
      {
        key: 'resourceGroupId',
        title: 'Res. Group',
        type: 'function',
        function: (value) => this.getGroupName(value),
      },
      {
        key: 'resourceTypeName',
        title: 'Res. Type',
      },
      {
        key: 'resourceTypePermission',
        title: 'Res. Type Cat.',
      },
    ];
  }
}
