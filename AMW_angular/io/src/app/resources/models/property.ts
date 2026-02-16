export type PropertyDescriptorOrigin = 'INSTANCE' | 'RELATION' | 'TYPE' | 'TYPE_REL';

export interface Property {
  name: string;
  value: string | null;
  replacedValue: string;
  generalComment: string;
  valueComment: string;
  context: string;
  definedInContext?: boolean;
  displayName?: string;
  validationRegex?: string;
  encrypted?: boolean;
  nullable?: boolean;
  optional?: boolean;
  defaultValue?: string;
  exampleValue?: string;
  mik?: string;
  propertyDescriptorOrigin?: PropertyDescriptorOrigin;
  descriptorId?: number;
  cardinality?: number;
  disabled?: boolean;
  originOfValue?: string;
  hasOverrideInLowerContext?: boolean;
}
