import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import {TagsComponent} from "./tags/tags.component";
import {SettingsRoutingModule} from "./settings-routing.module";
import {SettingsComponent} from "./settings.component";



@NgModule({
  declarations: [SettingsComponent, TagsComponent],
  imports: [
    CommonModule,
    SettingsRoutingModule
  ]
})
export class SettingsModule { }
