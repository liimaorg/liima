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

export interface ComparedTemplate {
  path: string;
  original?: GeneratedTemplate;
  compared?: GeneratedTemplate;
  sameContent: boolean;
}

export interface ComparedApplication {
  applicationName: string;
  templates: ComparedTemplate[];
  originalErrors: string[];
  comparedErrors: string[];
}

export interface ComparedNode {
  nodeName: string;
  asTemplates: ComparedTemplate[];
  applications: ComparedApplication[];
  originalErrors: string[];
  comparedErrors: string[];
}

export interface ComparedGenerationResult {
  applicationServerName: string;
  originalReleaseName: string;
  comparedReleaseName: string;
  nodes: ComparedNode[];
  originalError?: string;
  comparedError?: string;
}
