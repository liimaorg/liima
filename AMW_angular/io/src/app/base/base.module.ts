import { NgModule } from '@angular/core';
import { BaseService } from './base.service';
import {NgbModule} from "@ng-bootstrap/ng-bootstrap";

@NgModule({
  imports: [NgbModule],
  declarations: [],
  providers: [ BaseService ]
})
export class BaseModule {}
