import { Routes } from '@angular/router';
import { DeploymentsComponent } from './deployments/deployments.component';
import { appsRoutes } from './apps/apps.routes';
import { auditviewRoutes } from './auditview/auditview.routes';
import { deploymentRoutes } from './deployment/deployment-routes';
import { settingsRoutes } from './settings/settings.routes';
import { deploymentsRoutes } from './deployments/deployments.routes';
import { serversRoute } from './servers/servers.route';
import { resourcesRoute } from './resources/resources.route';

export const routes: Routes = [
  // default route only, the rest is done in module routing
  { path: '', component: DeploymentsComponent },

  ...appsRoutes,
  ...serversRoute,
  ...resourcesRoute,
  ...settingsRoutes,
  ...auditviewRoutes,
  ...deploymentRoutes,
  ...deploymentsRoutes,
];
