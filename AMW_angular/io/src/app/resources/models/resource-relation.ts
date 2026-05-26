export interface ResourceRelation {
  id: number;
  slaveId: number;
  relatedResourceName: string;
  type: string;
  relatedResourceRelease: string;
  relationName: string;
  relationType: 'consumed' | 'provided';
  templates?: Template[];
  availableReleases?: RelationRelease[];
}

export interface Template {
  id: number;
  name: string;
  relatedResourceIdentifier?: string;
}

export interface RelationRelease {
  relationId: number;
  releaseName: string;
}

export interface UnresolvedRelation {
  resRelTypeId?: number;
  type: string;
  name: string;
}

export interface GroupedResourceRelations {
  runtime: ResourceRelation[];
  consumed: ResourceRelation[];
  provided: ResourceRelation[];
  unresolved: UnresolvedRelation[];
}
