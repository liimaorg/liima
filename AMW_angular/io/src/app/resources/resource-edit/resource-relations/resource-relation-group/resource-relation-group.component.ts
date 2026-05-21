import { Component, input, output } from '@angular/core';

export interface RelationGroupItem {
  key: string | number;
  name: string;
  type: string;
  release?: string;
  identifier?: string;
  unresolved?: boolean;
}

@Component({
  selector: 'app-resource-relation-group',
  standalone: true,
  templateUrl: './resource-relation-group.component.html',
  styleUrl: './resource-relation-group.component.scss',
})
export class ResourceRelationGroupComponent {
  title = input.required<string>();
  items = input.required<RelationGroupItem[]>();
  selectedKey = input<string | number | null>(null);
  itemSelected = output<RelationGroupItem>();

  selectItem(item: RelationGroupItem) {
    this.itemSelected.emit(item);
  }
}
