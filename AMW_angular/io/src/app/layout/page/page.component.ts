import { Component } from '@angular/core';

import { ReactiveFormsModule } from '@angular/forms';

@Component({
  selector: 'app-page',
  imports: [ReactiveFormsModule],
  template: `
    <div class="row page-header-row">
      <div class="col-12 bg-dark-subtle py-2">
        <h4 data-cy="page-title"><ng-content select=".page-title"></ng-content></h4>
      </div>
    </div>
    <div class="container">
      <ng-content select=".page-content"></ng-content>
    </div>
  `,
  styles: `
    :host {
      display: block;
    }

    .page-header-row {
      position: fixed;
      top: 50px; /* Fixed below navbar (navbar is exactly 50px height) */
      left: 0;
      right: 0;
      margin-left: 0; /* Reset margins */
      margin-right: 0; /* Reset margins */
      z-index: 998; /* Below navbar but above content */
      background-color: var(--bs-gray-100);
      border-bottom: 1px solid var(--bs-border-color);
    }

    /* Fix content padding to account for fixed page header */
    .container {
      padding-top: 80px; /* 50px navbar + ~30px header */
    }

    .container {
      padding-top: 0;
    }
  `,
})
export class PageComponent {}
