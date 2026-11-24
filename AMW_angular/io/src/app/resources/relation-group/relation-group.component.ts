import { Component, computed, input, output } from '@angular/core';

export interface RelationGroupItem {
  key: string | number | undefined;
  name: string;
  type: string;
  release?: string;
  identifier?: string;
  unresolved?: boolean;
}

@Component({
  selector: 'app-relation-group',
  standalone: true,
  templateUrl: './relation-group.component.html',
  styleUrl: './relation-group.component.scss',
})
export class RelationGroupComponent {
  title = input.required<string>();
  items = input.required<RelationGroupItem[]>();
  selectedKey = input<string | number | null>(null);
  itemSelected = output<RelationGroupItem>();

  protected itemGroups = computed(() => {
    const groups = new Map<string, RelationGroupItem[]>();
    for (const item of this.items()) {
      const items = groups.get(item.type);
      if (items) {
        items.push(item);
      } else {
        groups.set(item.type, [item]);
      }
    }
    return Array.from(groups, ([type, items]) => ({ type, items }));
  });

  selectItem(item: RelationGroupItem) {
    this.itemSelected.emit(item);
  }
}
