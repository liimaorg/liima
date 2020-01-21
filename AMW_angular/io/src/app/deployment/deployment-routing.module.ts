import { RouterModule } from '@angular/router';
import { DeploymentComponent } from './deployment.component';
import { DeploymentsComponent } from './deployments.component';
import { NgModule } from '@angular/core';
import {NgbModule} from "@ng-bootstrap/ng-bootstrap";

@NgModule({
  imports: [RouterModule.forChild([
    {path: 'deployment', component: DeploymentComponent},
    {path: 'deployment/:deploymentId', component: DeploymentComponent},
    {path: 'deployment/:appserverName/:releaseName', component: DeploymentComponent},
    {path: 'deployments', component: DeploymentsComponent},
  ]), NgbModule],
  exports: [RouterModule]
})
export class DeploymentRoutingModule {
}
