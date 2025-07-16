export interface ResourceFunction {
  id: number;
  name: string;
  miks: Set<string>;
  content: string;
  definedOnResource: boolean;
  definedOnResourceType: boolean;
  isOverwritingFunction: boolean;
  overwrittenParentName?: string;
  functionOriginResourceName?: string;
}
