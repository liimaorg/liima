import { Component, Input } from '@angular/core';
@Component({
  selector: 'app-panel-heading',
  template: `
    <div *ngIf="name" class="panel-heading clearfix">{{ name }}</div>
  `,
  styleUrls: ['./panel-heading.component.scss']
})
export class PanelHeadingComponent {

  @Input() name: string;

  constructor() { }

}
