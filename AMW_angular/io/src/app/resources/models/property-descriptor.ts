import { PropertyType } from '../../settings/property-types/property-type';
import { PropertyTag } from '../../settings/property-types/property-tag';

export interface PropertyDescriptor {
  id?: number;
  name: string;
  displayName?: string;
  encrypted?: boolean;
  nullable?: boolean;
  optional?: boolean;
  validationRegex?: string;
  comment: string; // is it this comment?
  mik?: string;
  defaultValue?: string;
  exampleValue?: string;
  propertyTypeEntity: PropertyType;
  propertyTags: PropertyTag[];
}
