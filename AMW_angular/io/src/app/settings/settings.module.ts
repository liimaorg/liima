import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TagsComponent } from './tags/tags.component';
import { SettingsRoutingModule } from './settings-routing.module';
import { SettingsComponent } from './settings.component';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { NgSelectModule } from '@ng-select/ng-select';

import { PermissionComponent } from './permission/permission.component';
import { RestrictionEditComponent } from './permission/restriction-edit.component';
import { RestrictionListComponent } from './permission/restriction-list.component';
import { RestrictionAddComponent } from './permission/restriction-add.component';
import { PermissionService } from './permission/permission.service';
import { NgbNav, NgbNavItem, NgbNavModule } from '@ng-bootstrap/ng-bootstrap';

@NgModule({
  imports: [
    CommonModule,
    SettingsRoutingModule,
    ReactiveFormsModule,
    FormsModule,
    NgSelectModule,
    NgbNavModule,
    NgbNav,
    NgbNavItem,
    SettingsComponent,
    TagsComponent,
    PermissionComponent,
    RestrictionEditComponent,
    RestrictionListComponent,
    RestrictionAddComponent,
  ],
  providers: [PermissionService],
})
export class SettingsModule {}
