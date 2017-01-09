import { Release } from './release';

export interface Resource {
  id: number;
  name: string;
  type: string;
  release: Release;
  releases: Release[];
}
