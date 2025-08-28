import { Component, Input, OnChanges } from '@angular/core';

@Component({
  selector: 'app-icon',
  template: ` <svg class="icon" width="16" height="16" fill="currentColor">
    <use [attr.xlink:href]="iconPath" />
  </svg>`,
  styleUrl: './icon.component.css',
  standalone: true,
})
export class IconComponent implements OnChanges {
  @Input()
  icon: string;

  iconPath: string;

  ngOnChanges(): void {
    this.iconPath = 'bootstrap-icons.svg#' + this.icon;
  }
}
