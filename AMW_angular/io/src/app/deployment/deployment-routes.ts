import { DeploymentComponent } from './deployment.component';

const DEPLOYMENT_TITLE = 'Deployment - Liima';
export const deploymentRoutes = [
  { path: 'deployment', component: DeploymentComponent, title: DEPLOYMENT_TITLE },
  { path: 'deployment/:deploymentId', component: DeploymentComponent, title: DEPLOYMENT_TITLE },
  {
    path: 'deployment/:appserverName/:releaseName',
    component: DeploymentComponent,
    title: DEPLOYMENT_TITLE,
  },
];
