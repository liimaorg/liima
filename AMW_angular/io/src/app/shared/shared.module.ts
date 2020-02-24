import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SortedColumnComponent } from './sorted-column/sorted-column.component';
import { PageHeaderComponent } from './elements/page-header.component';
import { LoadingIndicatorComponent } from './elements/loading-indicator.component';

@NgModule({
  declarations: [
    SortedColumnComponent,
    PageHeaderComponent,
    LoadingIndicatorComponent
  ],
  imports: [CommonModule],
  exports: [
    SortedColumnComponent,
    PageHeaderComponent,
    LoadingIndicatorComponent
  ]
})
export class SharedModule {}
