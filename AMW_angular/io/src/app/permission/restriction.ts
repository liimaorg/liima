import { Restriction } from './restriction';

export interface Restriction {
  id: number;
  roleName: string;
  userName: string;
  permission: string;
  resourceGroupId: number;
  resourceTypeName: string;
  resourceTypePermission: string;
  contextName: string;
  action: string;
}
