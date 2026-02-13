export interface GeneratedTemplate {
  name: string;
  path: string;
  content: string;
  errors: string[];
}

export interface ApplicationGenerationResult {
  applicationName: string;
  templates: GeneratedTemplate[];
  errors: string[];
}

export interface NodeGenerationResult {
  nodeName: string;
  asTemplates: GeneratedTemplate[];
  appResults: ApplicationGenerationResult[];
  errors: string[];
}

export interface EnvironmentGenerationResult {
  releaseName: string;
  applicationServerName: string;
  nodeGenerationResults: NodeGenerationResult[];
  error: string;
}
