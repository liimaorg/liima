import { Component, OnInit, Input } from '@angular/core';

@Component({
  selector: 'app-sort-direction',
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
export class SortedColumnComponent implements OnInit {
  @Input() sortDirection: string;

  constructor() {}

  ngOnInit(): void {}
}
