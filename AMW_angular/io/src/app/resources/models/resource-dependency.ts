export interface ResourceDependency {
  resourceId: number;
  resourceName: string;
  resourceTypeName: string;
  releaseName: string;
}

export interface ResourceDependencies {
  resourceName: string;
  consumedRelations: ResourceDependency[];
  providedRelations: ResourceDependency[];
}
