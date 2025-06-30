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
}

export interface EntryActionOutput {
  action: EntryAction;
  id: number;
}
@Component({
  selector: 'app-table',
  templateUrl: './table.component.html',
  styleUrl: './table.component.scss',
  imports: [ButtonComponent, IconComponent, DatePipe]
})
export class TableComponent<T> {
  entityName = input.required<string>();
  headers = input.required<TableColumnType<T>[]>();
  dataCyNameKey = input<keyof T>();
  readonlyFlag = input<keyof T>();
  data = input.required<any[]>();
  canEdit = input<boolean>(false);
  canDelete = input<boolean>(false);
  fixed = input<boolean>(true);
  hasAction = computed(() => this.canEdit() || this.canDelete());
  edit = output<EntryActionOutput>();
  delete = output<EntryActionOutput>();
  protected readonly EntryAction = EntryAction;
  dateFormat = DATE_FORMAT;

  getTotalColspan() {
    return this.headers().length + (this.hasAction() ? 1 : 0);
  }

  getIcon(cellValue: any, header: TableColumnType): string | undefined {
    const mapping = header.iconMapping?.find((m) => m.value === cellValue);
    return mapping?.icon;
  }

  get tableClass() {
    let base = 'table table-sm table-striped w-100';
    if (this.fixed()) base += ' table-fixed';
    return base;
  }
}
