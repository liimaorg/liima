export interface ResourceTemplate {
  id: number | null;
  relatedResourceIdentifier: string;
  name: string;
  targetPath: string | null;
  targetPlatforms: string[];
  fileContent: string;
  sourceType?: 'RESOURCE' | 'RESOURCE_TYPE';
  version: number;
}
