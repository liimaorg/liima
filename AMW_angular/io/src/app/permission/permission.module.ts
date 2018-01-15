import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
// import { TagsInputModule } from 'ngx-tags-input/dist';
import { NgxTypeaheadModule } from 'ngx-typeahead';
import { PermissionComponent } from './permission.component';
import { PermissionService } from './permission.service';
import { PermissionRoutingModule } from './permission-routing.module';
import { RestrictionEditComponent } from './restriction-edit.component';
import { RestrictionListComponent } from './restriction-list.component';
import { RestrictionAddComponent } from './restriction-add.component';

@NgModule({
  imports: [ CommonModule, FormsModule, NgxTypeaheadModule, PermissionRoutingModule ],
  declarations: [ PermissionComponent, RestrictionEditComponent, RestrictionListComponent, RestrictionAddComponent ],
  providers: [ PermissionService ]
})
export class PermissionModule {
}
