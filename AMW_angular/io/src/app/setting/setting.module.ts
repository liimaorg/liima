import { NgModule } from '@angular/core';
import { SettingService } from './setting.service';
import {NgbModule} from "@ng-bootstrap/ng-bootstrap";

@NgModule({
  imports: [NgbModule],
  declarations: [],
  providers: [ SettingService ]
})
export class SettingModule {}
