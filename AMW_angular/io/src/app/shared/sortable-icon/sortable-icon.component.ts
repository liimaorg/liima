import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-sortable-icon',
  template: `
    <span
      [ngClass]="[
        'glyphicon',
        sortDirection === 'DESC'
          ? 'glyphicon-triangle-bottom'
          : 'glyphicon-triangle-top'
      ]"
    ></span>
  `,
  styles: []
})
export class SortableIconComponent {
  @Input() sortDirection: SortDirection;

  constructor() {}
}

type SortDirection = 'ASC' | 'DESC';
