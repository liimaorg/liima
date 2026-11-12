export interface RestrictionsCreation {
  roleName: string | null;
  userNames: string[];
  permissionNames: string[];
  resourceGroupIds: number[];
  resourceTypeNames: string[];
  resourceTypePermission: string | null;
  contextNames: string[];
  actions: string[];
}
