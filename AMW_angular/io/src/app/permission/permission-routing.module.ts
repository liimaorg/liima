import { RouterModule } from '@angular/router';
import { PermissionComponent } from './permission.component';
import { NgModule } from '@angular/core';
import { PageNotFoundComponent } from '../not-found.component';

@NgModule({
  imports: [RouterModule.forChild([
    {path: 'permission', component: PermissionComponent},
    {path: 'permission/role', component: PermissionComponent},
    {path: 'permission/user', component: PermissionComponent},
    {path: '**', component: PageNotFoundComponent},
  ])],
  exports: [RouterModule]
})
export class PermissionRoutingModule {
}
