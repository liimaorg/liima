export interface ResourceRelation {
  id: number;
  slaveId: number;
  relatedResourceName: string; // slaveName
  type: string; // slaveTypeName
  relatedResourceRelease: string; // slaveRelease
  relationName: string;
  relationType: 'consumed' | 'provided'; // FIXME what about runtime
  templates?: Template[];
  availableReleases?: RelationRelease[];
  identifier?: string;
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
  identifier?: string;
}

export interface GroupedResourceRelations {
  runtime: ResourceRelation[];
  consumed: ResourceRelation[];
  provided: ResourceRelation[];
  unresolved: UnresolvedRelation[];
}
