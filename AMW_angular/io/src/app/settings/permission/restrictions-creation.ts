export interface RestrictionsCreation {
  roleName: string;
  userNames: string[];
  permissionNames: string[];
  resourceGroupIds: number[];
  resourceTypeNames: string[];
  resourceTypePermission: string;
  contextNames: string[];
  actions: string[];
}
