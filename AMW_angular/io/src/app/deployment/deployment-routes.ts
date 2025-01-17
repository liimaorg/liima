import { DeploymentComponent } from './deployment.component';

export const deploymentRoutes = [
  { path: 'deployment', component: DeploymentComponent, title: 'Deployment - Liima' },
  { path: 'deployment/:deploymentId', component: DeploymentComponent, title: 'Deployment - Liima' },
  {
    path: 'deployment/:appserverName/:releaseName',
    component: DeploymentComponent,
    title: 'Deployment - Liima',
  },
];
