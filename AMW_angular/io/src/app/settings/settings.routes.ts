import { TagsComponent } from './tags/tags.component';
import { SettingsComponent } from './settings.component';
import { PermissionComponent } from './permission/permission.component';
import { ApplicationInfoComponent } from './application-info/application-info.component';
import { ReleasesComponent } from './releases/releases.component';
import { DeploymentParameterComponent } from './deployment-parameter/deployment-parameter.component';
import { PropertyTypesComponent } from './property-types/property-types.component';
import { FunctionsComponent } from './functions/functions.component';
import { EnvironmentsPageComponent } from './environments/environments-page.component';
import { Routes } from '@angular/router';

export const settingsRoutes: Routes = [
  {
    path: 'settings',
    component: SettingsComponent,
    title: 'Settings - Liima',
    children: [
      { path: '', redirectTo: 'environments', pathMatch: 'full' },
      { path: 'releases', component: ReleasesComponent },
      { path: 'tags', component: TagsComponent },
      {
        path: 'permission/delegation/:actingUser',
        component: PermissionComponent,
      },
      { path: 'permission/:restrictionType', component: PermissionComponent },
      { path: 'permission', component: PermissionComponent },
      { path: 'application-info', component: ApplicationInfoComponent },
      { path: 'deployment-parameter', component: DeploymentParameterComponent },
      { path: 'property-types', component: PropertyTypesComponent },
      { path: 'functions', component: FunctionsComponent },
      { path: 'environments', component: EnvironmentsPageComponent },
    ],
  },
];
