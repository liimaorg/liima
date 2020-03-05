import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SortedColumnComponent } from './sorted-column/sorted-column.component';
import { PageHeaderComponent } from './elements/page-header.component';

import { LoadingIndicatorComponent } from './elements/loading-indicator.component';
import { PageNotFoundComponent } from './page-not-found.component';

@NgModule({
  declarations: [
    SortedColumnComponent,
    PageHeaderComponent,
    PageNotFoundComponent,
    LoadingIndicatorComponent
  ],
  imports: [CommonModule],
  exports: [
    SortedColumnComponent,
    PageHeaderComponent,
    PageNotFoundComponent,
    LoadingIndicatorComponent
  ]
})
export class SharedModule {}
