export interface Environment {
  id: number;
  name: string;
  nameAlias: string;
  parent: string;
  selected: boolean;
  disabled: boolean;
}

export interface EnvironmentTree {
  id: number;
  name: string;
  nameAlias: string;
  children: EnvironmentTree[];
  selected: boolean;
  disabled: boolean;
}
