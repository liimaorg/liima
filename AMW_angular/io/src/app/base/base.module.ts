import { NgModule } from '@angular/core';
import { BaseService } from './base.service';
import { PanelHeadingComponent } from './panel-heading/panel-heading.component';
import {CommonModule} from "@angular/common";

@NgModule({
  imports: [
    CommonModule
  ],
  declarations: [PanelHeadingComponent],
  exports: [PanelHeadingComponent],
  providers: [BaseService]
})
export class BaseModule { }
