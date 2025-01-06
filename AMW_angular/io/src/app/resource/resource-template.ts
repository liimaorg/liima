export interface ResourceTemplate {
  id: number;
  relatedResourceIdentifier: string;
  name: string;
  targetPath: string;
  targetPlatforms: string[];
  fileContent: string;
  sourceType?: 'RESOURCE' | 'RESOURCE_TYPE';
}
