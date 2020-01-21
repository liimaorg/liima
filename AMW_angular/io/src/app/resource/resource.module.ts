import { NgModule } from '@angular/core';
import { ResourceService } from './resource.service';
import {NgbModule} from "@ng-bootstrap/ng-bootstrap";

@NgModule({
  imports: [NgbModule],
  declarations: [],
  providers: [ ResourceService ]
})
export class ResourceModule {}
