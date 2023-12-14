import { Routes } from '@angular/router';
import { DeploymentsComponent } from './deployments/deployments.component';
import { SettingsComponent } from './settings/settings.component';
import { TagsComponent } from './settings/tags/tags.component';
import { auditviewRoutes } from './auditview/auditview.routes';
import { deploymentRoutes } from './deployment/deployment-routes';

export const routes: Routes = [
  // default route only, the rest is done in module routing
  { path: '', component: DeploymentsComponent },
  {
    path: 'settings',
    component: SettingsComponent,
    children: [{ path: 'tags', component: TagsComponent }],
  },
  ...auditviewRoutes,
  ...deploymentRoutes,
];
