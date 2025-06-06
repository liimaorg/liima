export interface ResourceType {
  id: number;
  name: string;
  hasChildren: boolean;
  children: ResourceType[];
  isApplication: boolean;
  isDefaultResourceType: boolean;
}
