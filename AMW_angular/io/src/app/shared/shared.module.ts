import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SortedColumnComponent } from './sorted-column/sorted-column.component';
import { PageHeaderComponent } from './elements/page-header.component';

@NgModule({
  declarations: [SortedColumnComponent, PageHeaderComponent],
  imports: [CommonModule],
  exports: [SortedColumnComponent, PageHeaderComponent]
})
export class SharedModule {}
