import { RouterModule } from '@angular/router';
import { DeploymentsComponent } from './deployments.component';
import { NgModule } from '@angular/core';
import { LogsComponent } from './logs/logs.component';
import { DeploymentContainerComponent } from './deployment-container/deployment-container.component';

@NgModule({
  imports: [
    RouterModule.forChild([
      {
        path: 'deployments',
        component: DeploymentContainerComponent,
        children: [
          { path: '', component: DeploymentsComponent },
          { path: ':deploymentId/logs', component: LogsComponent },
        ],
      },
    ]),
  ],
  exports: [RouterModule],
})
export class DeploymentsRoutingModule {}
