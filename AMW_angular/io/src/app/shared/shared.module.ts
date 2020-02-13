import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SortedColumnComponent } from './sorted-column/sorted-column.component';

@NgModule({
  declarations: [SortedColumnComponent],
  imports: [CommonModule],
  exports: [SortedColumnComponent]
})
export class SharedModule {}
