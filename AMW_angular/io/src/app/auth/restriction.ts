import { Permission } from './permission';

export enum Action {
  READ = 'READ',
  CREATE = 'CREATE',
  UPDATE = 'UPDATE',
  DELETE = 'DELETE',
  ALL = 'ALL',
}

export enum ResourceTypeCategory {
  DEFAULT_ONLY = 'DEFAULT_ONLY',
  NON_DEFAULT_ONLY = 'NON_DEFAULT_ONLY',
  ANY = 'ANY'
}

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
