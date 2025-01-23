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

export class EnvironmentTreeUtils {
  static searchById(tree: EnvironmentTree[], id: number): EnvironmentTree | undefined {
    for (const node of tree) {
      if (node.id === id) {
        return node;
      }
      if (node.children && node.children.length > 0) {
        const result = EnvironmentTreeUtils.searchById(node.children, id); // Recursive call
        if (result) {
          return result;
        }
      }
    }
    return undefined;
  }
}
