import {Component, Input, OnChanges} from '@angular/core';

@Component({
  selector: 'app-sortable-icon',
  template: ` <app-icon icon="caret-{{ direction }}-fill"></app-icon> `,
  styles: [],
})
export class SortableIconComponent implements OnChanges{
  @Input() sortDirection: SortDirection;

  direction: string;

  ngOnChanges() {
    this.direction = this.sortDirection === 'ASC' ? 'up' : 'down';
  }
}

type SortDirection = 'ASC' | 'DESC';
