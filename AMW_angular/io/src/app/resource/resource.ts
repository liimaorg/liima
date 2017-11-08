import { Release } from './release';

export interface Resource {
  id: number;
  resourceGroupId: number;
  name: string;
  type: string;
  version: string;
  release: Release;
  releases: Release[];
}
