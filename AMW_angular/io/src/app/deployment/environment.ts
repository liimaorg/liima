export interface Environment {
  id: number;
  name: string;
  nameAlias: string;
  parent: string;
  selected: boolean;
  disabled: boolean;
}

export class EnvironmentTree {
  id: number;
  name: string;
  nameAlias: string;
  children: EnvironmentTree[];
  selected: boolean;
  disabled: boolean;

  constructor(
    id: number,
    name: string,
    nameAlias: string,
    children: EnvironmentTree[],
    selected: boolean,
    disabled: boolean,
  ) {
    this.id = id;
    this.name = name;
    this.nameAlias = nameAlias;
    this.children = children;
    this.selected = selected;
    this.disabled = disabled;
  }
}
