import { Directive, EventEmitter, Output, input, model } from '@angular/core';

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
  // eslint-disable-next-line @angular-eslint/directive-selector
  selector: 'th[sortable]',
  host: {
    '[class.asc]': 'direction() === "asc"',
    '[class.desc]': 'direction() === "desc"',
    '(click)': 'rotate()',
  },
  standalone: true,
})
export class SortableHeader {
  sortable = input.required<string>();
  direction = model<SortDirection>('');
  @Output() sort = new EventEmitter<SortEvent>();

  sortableValue(): string {
    return this.sortable();
  }

  rotate() {
    const currentDirection = this.direction();
    const newDirection = rotate[currentDirection];
    this.sort.emit({ column: this.sortableValue(), direction: newDirection });
  }
}
