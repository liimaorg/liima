import { Directive, EventEmitter, Input, Output, input } from '@angular/core';

export type SortDirection = 'asc' | 'desc' | '';
const rotate: { [key: string]: SortDirection } = {
  asc: 'desc',
  desc: '',
  '': 'asc',
};

export interface SortEvent {
  column: string;
  direction: SortDirection;
}

@Directive({
  selector: 'th[sortable]',
  host: {
    '[class.asc]': 'direction === "asc"',
    '[class.desc]': 'direction === "desc"',
    '(click)': 'rotate()',
  },
  standalone: true,
})
export class SortableHeader {
  sortable = input.required<string>();
  @Input() direction: SortDirection = '';
  @Output() sort = new EventEmitter<SortEvent>();

  sortableValue(): string {
    return this.sortable();
  }

  rotate() {
    this.direction = rotate[this.direction];
    this.sort.emit({ column: this.sortableValue(), direction: this.direction });
  }
}
