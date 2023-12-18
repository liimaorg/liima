import { RouterModule } from '@angular/router';
import { NgModule } from '@angular/core';
import { TagsComponent } from './tags/tags.component';
import { SettingsComponent } from './settings.component';
import { PermissionComponent } from './permission/permission.component';

@NgModule({
  imports: [
    RouterModule.forChild([
      {
        path: 'settings',
        component: SettingsComponent,
        title: 'Settings',
        children: [
          { title: 'Settings - Tags', path: 'tags', component: TagsComponent },
          {
            path: 'permission/delegation/:actingUser',
            component: PermissionComponent,
          },
          { path: 'permission/:restrictionType', component: PermissionComponent },
          { path: 'permission', component: PermissionComponent },
        ],
      },
    ]),
  ],
  exports: [RouterModule],
})
export class SettingsRoutingModule {}
