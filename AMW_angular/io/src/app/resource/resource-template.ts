export interface ResourceTemplate {
  id: number;
  relatedResourceIdentifier: string;
  name: string;
  targetPath: string;
  targetPlatforms: string[];
  fileContent: string;
}
