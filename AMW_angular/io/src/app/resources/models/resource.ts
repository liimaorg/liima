import { Release } from './release';

export interface Resource {
  id: number;
  name: string;
  type: string;
  version: string;
  defaultRelease: Release;
  releases: Release[];
  defaultResourceId?: number;
  resourceGroupId?: number;
  resourceTypeId?: number;
}
