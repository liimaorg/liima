export interface Environment {
  id: number;
  name: string;
  nameAlias: string;
  parent: string; //TODO Rename to parentName
  parentId: number;
  selected: boolean;
  disabled: boolean;
}

export interface EnvironmentTree {
  id: number;
  name: string;
  nameAlias: string;
  parent: string; //TODO Rename to parentName
  parentId: number;
  children: EnvironmentTree[];
  selected: boolean;
  disabled: boolean;
}
