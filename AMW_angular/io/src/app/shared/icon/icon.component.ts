import {Component, Input, OnChanges} from '@angular/core';

@Component({
  selector: 'app-icon',
  template: ` <svg class="bi" width="16" height="16" fill="currentColor">
    <use [attr.xlink:href]="iconPath" />
  </svg>`,
  styles: [],
})
export class IconComponent implements OnChanges {
  @Input()
  icon: string;

  iconPath: string;
  constructor() {}

  ngOnChanges(): void {
    this.iconPath = 'bootstrap-icons.svg#' + this.icon;
  }
}
