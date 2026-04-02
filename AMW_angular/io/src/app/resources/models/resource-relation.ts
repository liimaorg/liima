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

export interface GroupedRelations {
  consumed: ResourceRelation[];
  provided: ResourceRelation[];
}
