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

  selectItem(item: RelationGroupItem) {
    this.itemSelected.emit(item);
  }
}
