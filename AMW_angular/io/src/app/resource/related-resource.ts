import { ResourceTemplate } from './resource-template';

export interface RelatedResource {
  relatedResourceName: string;
  type: string;
  relatedResourceRelease: string;
  relationName: string;
  relationType: string;
  templates: ResourceTemplate;
}
