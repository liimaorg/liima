import { Permission } from './permission';

export type Action = 'READ' | 'CREATE' | 'UPDATE' | 'DELETE' | 'ALL';

export type ResourceTypeCategory = 'DEFAULT_ONLY' | 'NON_DEFAULT_ONLY' | 'ANY';

export interface Restriction {
  id: number;
  roleName: string;
  userName: string;
  permission: Permission;
  // only one of resourceGroupId, resourceTypeName and resourceTypePermission can be set
  resourceGroupId: number;
  resourceTypeName: string;
  resourceTypePermission: ResourceTypeCategory;
  contextName: string;
  action: Action;
}
