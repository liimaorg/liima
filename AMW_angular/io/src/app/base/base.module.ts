import { NgModule } from '@angular/core';
import { BaseService } from './base.service';
import { CommonModule } from '@angular/common';

@NgModule({
  imports: [CommonModule],
  declarations: [],
  exports: [],
  providers: [BaseService],
})
export class BaseModule {}
