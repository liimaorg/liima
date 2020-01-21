import { RouterModule } from '@angular/router';
import { PermissionComponent } from './permission.component';
import { NgModule } from '@angular/core';
import { PageNotFoundComponent } from '../not-found.component';
import {NgbModule} from "@ng-bootstrap/ng-bootstrap";

@NgModule({
  imports: [RouterModule.forChild([
    {path: 'permission/delegation/:actingUser', component: PermissionComponent},
    {path: 'permission/:restrictionType', component: PermissionComponent},
    {path: 'permission', component: PermissionComponent},
    {path: '**', component: PageNotFoundComponent},
  ]), NgbModule],
  exports: [RouterModule]
})
export class PermissionRoutingModule {
}
