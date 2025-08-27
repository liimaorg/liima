import { Component } from '@angular/core';

import { ReactiveFormsModule } from '@angular/forms';

@Component({
  selector: 'app-page',
  imports: [ReactiveFormsModule],
  template: `
    <div class="row">
      <div class="col-12 bg-dark-subtle pt-2">
        <h4 data-cy="page-title"><ng-content select=".page-title"></ng-content></h4>
      </div>
    </div>
    <div class="container mt-4">
      <ng-content select=".page-content"></ng-content>
    </div>
  `,
  styles: ``,
})
export class PageComponent {}
