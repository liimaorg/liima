import { RouterModule } from '@angular/router';
import { PermissionComponent } from './permission.component';
import { PermissionDelegationComponent } from './permission-delegation.component';
import { NgModule } from '@angular/core';
import { PageNotFoundComponent } from '../not-found.component';

@NgModule({
  imports: [RouterModule.forChild([
    {path: 'permission/delegation/:actingUser', component: PermissionDelegationComponent},
    {path: 'permission/:restrictionType', component: PermissionComponent},
    {path: 'permission', component: PermissionComponent},
    {path: '**', component: PageNotFoundComponent},
  ])],
  exports: [RouterModule]
})
export class PermissionRoutingModule {
}
