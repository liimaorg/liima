import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TagsComponent } from './tags/tags.component';
import { SettingsRoutingModule } from './settings-routing.module';
import { SettingsComponent } from './settings.component';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { SharedModule } from '../shared/shared.module';

@NgModule({
  declarations: [SettingsComponent, TagsComponent],
  imports: [CommonModule, SettingsRoutingModule, ReactiveFormsModule, FormsModule, SharedModule],
})
export class SettingsModule {}
