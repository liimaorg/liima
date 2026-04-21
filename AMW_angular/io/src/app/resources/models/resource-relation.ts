export interface ResourceRelation {
  id: number;
  relatedResourceName: string;
  type: string;
  relatedResourceRelease: string;
  relationName: string;
  relationType: 'consumed' | 'provided';
  templates?: Template[];
}

export interface Template {
  id: number;
  name: string;
  relatedResourceIdentifier?: string;
}

export interface UnresolvedRelation {
  type: string;
  name: string;
}

export interface GroupedResourceRelations {
  runtime: ResourceRelation[];
  consumed: ResourceRelation[];
  provided: ResourceRelation[];
  unresolved: UnresolvedRelation[];
}
