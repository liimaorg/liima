import { Release } from './release';

export interface Relation {
  relatedResourceName: string;
  relatedResourceRelease: Release;
  identifier: string;
}
