import { Property } from './property';
import { Relation } from './relation';
import { ResourceTag } from './resource-tag';

export interface Release {
  id: number;
  name?: string;
  release?: string;
  installationInProductionAt: number;
  relations?: Relation[];
  properties?: Property[];
  resourceTags?: ResourceTag[];
}
