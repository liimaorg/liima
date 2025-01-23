import { Component, computed, input, output } from '@angular/core';
import { ButtonComponent } from '../button/button.component';
import { IconComponent } from '../icon/icon.component';
import { EntryAction, TileListEntryOutput } from '../tile/tile-list/tile-list.component';

export interface TableHeader {
  key: string;
  title: string;
  type?: 'badge';
  nested?: TableHeader;
}

@Component({
  selector: 'app-table',
  templateUrl: './table.component.html',
  standalone: true,
  imports: [ButtonComponent, IconComponent],
})
export class TableComponent {
  headers = input.required<TableHeader[]>();
  data = input.required<any[]>();
  canEdit = input<boolean>(false);
  canDelete = input<boolean>(false);
  hasAction = computed(() => this.canEdit() || this.canDelete());
  edit = output<TileListEntryOutput>();
  delete = output<TileListEntryOutput>();

  protected readonly EntryAction = EntryAction;
}
