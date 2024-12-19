import { PropertyTag } from './property-tag';

export interface PropertyType {
  id: number;
  name: string;
  encrypted: boolean;
  validationRegex: string;
  propertyTags: PropertyTag[];
}
