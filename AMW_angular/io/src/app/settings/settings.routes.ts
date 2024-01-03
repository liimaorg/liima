import { TagsComponent } from './tags/tags.component';
import { SettingsComponent } from './settings.component';
import { PermissionComponent } from './permission/permission.component';
import { ApplicationInfoComponent } from './application-info/application-info.component';

export const settingsRoutes = [
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
      { path: 'application-info', component: ApplicationInfoComponent },
    ],
  },
];
