import { Property } from './property';
import { Relation } from './relation';

export interface Release {
  id: number;
  release: string;
  relations: Relation[];
  properties: Property[];
}
