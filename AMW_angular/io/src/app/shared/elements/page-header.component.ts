import { Component } from '@angular/core';

@Component({
  selector: 'app-page-header',
  template: `
    <div class="row page-header">
      <div class="col-md-4">
        <ng-content></ng-content>
      </div>
    </div>
  `,
  styleUrls: ['./page-header.component.scss']
})
export class PageHeaderComponent {
  constructor() {}
}
