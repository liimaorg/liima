import { PropertyTag } from './property-tag';

export interface PropertyType {
  id: number | null;
  name: string;
  encrypted: boolean;
  validationRegex: string;
  propertyTags: PropertyTag[];
}
