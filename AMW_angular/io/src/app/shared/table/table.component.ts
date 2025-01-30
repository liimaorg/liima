import { Component, computed, input, output } from '@angular/core';
import { ButtonComponent } from '../button/button.component';
import { IconComponent } from '../icon/icon.component';
import { DATE_FORMAT } from '../../core/amw-constants';
import { DatePipe } from '@angular/common';

export type TableCellType = 'badge-list' | 'date' | 'function' | 'icon';

export interface TableHeader<T = any> {
  key: keyof T;
  title: string;
  type?: TableCellType;
  iconMapping?: { value: any; icon: string }[];
  function?: (value: any) => string;
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
  imports: [ButtonComponent, IconComponent, DatePipe],
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
  dateFormat = DATE_FORMAT;

  getTotalColspan() {
    return this.headers().length + (this.hasAction() ? 1 : 0);
  }

  getIcon(cellValue: any, header: TableHeader): string | undefined {
    const mapping = header.iconMapping?.find((m) => m.value === cellValue);
    return mapping?.icon;
  }
}
