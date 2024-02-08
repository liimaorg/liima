export interface Restriction {
  role: string;
  permission: string;
  resourceType: string;
  resourceTypePermission: string;
  context: number;
  action: boolean;
}
