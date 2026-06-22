export interface ResourceType {
  id: number | null;
  name: string;
  hasChildren: boolean;
  hasParent: boolean;
  children: ResourceType[];
  isApplication: boolean;
  isDefaultResourceType: boolean;
}
