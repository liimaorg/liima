import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { PermissionComponent } from './permission.component';
import { PermissionService } from './permission.service';
import { PermissionRoutingModule } from './permission-routing.module';
import { RestrictionComponent } from './restriction.component';

@NgModule({
  imports: [ CommonModule, FormsModule, PermissionRoutingModule ],
  declarations: [ PermissionComponent, RestrictionComponent ],
  providers: [ PermissionService ]
})
export class PermissionModule {
}
