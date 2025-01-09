import { Property } from './property';
import { Relation } from './relation';
import { ResourceTag } from './resource-tag';

export interface Release {
  id: number;
  release: string;
  relations: Relation[];
  properties: Property[];
  resourceTags: ResourceTag[];
}
