export interface ResourceFunction {
  id: number | null;
  name: string;
  miks: Set<string>;
  content: string;
  definedOnResource: boolean;
  definedOnResourceType: boolean;
  isOverwritingFunction: boolean;
  overwrittenParentName?: string;
  functionOriginResourceName?: string;
}
