import { Component, computed, input, output } from '@angular/core';
import { ButtonComponent } from '../button/button.component';
import { IconComponent } from '../icon/icon.component';

export interface TableHeader {
  key: string;
  title?: string;
  type?: 'badge-list' | 'split';
  nested?: TableHeader[];
}

export enum EntryAction {
  edit = 'edit',
  delete = 'delete',
}

export interface EntryActionOutput {
  action: EntryAction;
  id: number;
}
@Component({
  selector: 'app-table',
  templateUrl: './table.component.html',
  standalone: true,
  imports: [ButtonComponent, IconComponent],
})
export class TableComponent {
  entityName = input.required<string>();
  headers = input.required<TableHeader[]>();
  data = input.required<any[]>();
  canEdit = input<boolean>(false);
  canDelete = input<boolean>(false);
  hasAction = computed(() => this.canEdit() || this.canDelete());
  edit = output<EntryActionOutput>();
  delete = output<EntryActionOutput>();
  protected readonly EntryAction = EntryAction;

  getTotalColspan() {
    return this.headers().length + (this.hasAction() ? 1 : 0) + this.totalSplitHeaders();
  }

  private totalSplitHeaders() {
    let total = 0;
    this.headers().forEach((header: TableHeader) => {
      if (header.type === 'split' && header.nested) {
        total += header.nested.length;
      }
    });
    return total;
  }
}
