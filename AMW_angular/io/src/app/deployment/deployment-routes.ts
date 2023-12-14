import { DeploymentComponent } from './deployment.component';

export const deploymentRoutes = [
  { path: 'deployment', component: DeploymentComponent },
  { path: 'deployment/:deploymentId', component: DeploymentComponent },
  {
    path: 'deployment/:appserverName/:releaseName',
    component: DeploymentComponent,
  },
];
