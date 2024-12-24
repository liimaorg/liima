export interface ResourceFunction {
  id: number;
  name: string;
  miks: string[];
  definedOnResource: boolean;
  definedOnResourceType: boolean;
  isOverwritingFunction: boolean;
  overwrittenParentName?: string;
  functionOriginResourceName?: string;
}
