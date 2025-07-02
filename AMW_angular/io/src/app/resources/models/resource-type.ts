export interface ResourceType {
  id: number;
  name: string;
  hasChildren: boolean;
  hasParent: boolean;
  children: ResourceType[];
  isApplication: boolean;
  isDefaultResourceType: boolean;
}
