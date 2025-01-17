import { DeploymentsComponent } from './deployments.component';
import { DeploymentContainerComponent } from './deployment-container/deployment-container.component';
import { DeploymentLogsComponent } from './logs/deployment-logs.component';

export const deploymentsRoutes = [
  {
    path: 'deployments',
    component: DeploymentContainerComponent,
    title: 'Deployments - Liima',
    children: [
      { path: '', component: DeploymentsComponent },
      { path: ':deploymentId/logs', component: DeploymentLogsComponent },
      { path: ':deploymentId/logs/:fileName', component: DeploymentLogsComponent },
    ],
  },
];
