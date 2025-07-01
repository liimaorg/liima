import { Component, computed, input, output } from '@angular/core';
import { ButtonComponent } from '../button/button.component';
import { IconComponent } from '../icon/icon.component';
import { DATE_FORMAT } from '../../core/amw-constants';
import { DatePipe } from '@angular/common';

export type TableCellType = 'badge-list' | 'date' | 'icon' | 'link';

export interface TableColumnType<T = any> {
  key: keyof T;
  columnTitle: string;
  cellType?: TableCellType;
  iconMapping?: { value: any; icon: string }[];
  urlKey?: keyof T;
  nameKey?: keyof T;
}

export enum EntryAction {
  edit = 'edit',
  delete = 'delete',
  navigate = 'navigate',
}

export interface EntryActionOutput {
  action: EntryAction;
  id: number;
}
@Component({
  selector: 'app-table',
  templateUrl: './table.component.html',
  styleUrl: './table.component.scss',
  standalone: true,
  imports: [ButtonComponent, IconComponent, DatePipe],
})
export class TableComponent<T> {
  entityName = input.required<string>();
  headers = input.required<TableColumnType<T>[]>();
  readonlyFlag = input<keyof T>();
  data = input.required<any[]>();
  canEdit = input<boolean>(false);
  canDelete = input<boolean>(false);
  canNavigate = input<boolean>(false);
  hasAction = computed(() => this.canEdit() || this.canDelete() || this.canNavigate());
  edit = output<EntryActionOutput>();
  delete = output<EntryActionOutput>();
  navigate = output<EntryActionOutput>();
  protected readonly EntryAction = EntryAction;
  dateFormat = DATE_FORMAT;

  getTotalColspan() {
    return this.headers().length + (this.hasAction() ? 1 : 0);
  }

  getIcon(cellValue: any, header: TableColumnType): string | undefined {
    const mapping = header.iconMapping?.find((m) => m.value === cellValue);
    return mapping?.icon;
  }
}
