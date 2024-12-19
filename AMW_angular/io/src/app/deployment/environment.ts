export interface Environment {
  id: number;
  name: string;
  nameAlias: string;
  parentName: string;
  parentId: number;
  selected: boolean;
  disabled: boolean;
}

export interface EnvironmentTree {
  id: number;
  name: string;
  nameAlias: string;
  parentName: string;
  parentId: number;
  children: EnvironmentTree[];
  selected: boolean;
  disabled: boolean;
}
