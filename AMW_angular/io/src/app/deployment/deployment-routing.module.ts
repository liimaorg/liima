import { RouterModule } from '@angular/router';
import { DeploymentComponent } from './deployment.component';
import { NgModule } from '@angular/core';

@NgModule({
  imports: [RouterModule.forChild([
    {path: 'deployment', component: DeploymentComponent},
    {path: 'deployment/:deploymentId', component: DeploymentComponent},
    {path: 'deployment/:appserverName/:releaseName', component: DeploymentComponent},
  ])],
  exports: [RouterModule]
})
export class DeploymentRoutingModule {
}
