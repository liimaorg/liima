import { Routes } from '@angular/router';
import { DeploymentsComponent } from './deployments/deployments.component';
import { auditviewRoutes } from './auditview/auditview.routes';
import { deploymentRoutes } from './deployment/deployment-routes';
import { settingsRoutes } from './settings/settings.routes';
import { deploymentsRoutes } from './deployments/deployments.routes';

export const routes: Routes = [
  // default route only, the rest is done in module routing
  { path: '', component: DeploymentsComponent },

  ...settingsRoutes,
  ...auditviewRoutes,
  ...deploymentRoutes,
  ...deploymentsRoutes,
];
