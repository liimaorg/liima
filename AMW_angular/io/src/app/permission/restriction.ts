import { Restriction } from './restriction';
import { Permission } from './permission';

export interface Restriction {
  id: number;
  roleName: string;
  userName: string;
  permission: Permission;
  resourceGroupId: number;
  resourceTypeName: string;
  resourceTypePermission: string;
  contextName: string;
  action: string;
}
