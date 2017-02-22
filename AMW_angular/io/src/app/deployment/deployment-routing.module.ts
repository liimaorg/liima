import { RouterModule } from '@angular/router';
import { DeploymentComponent } from './deployment.component';
import { NgModule } from '@angular/core';
import { PageNotFoundComponent } from '../not-found.component';

@NgModule({
  imports: [RouterModule.forChild([
    {path: 'deployment', component: DeploymentComponent},
    {path: 'deployment/:deploymentId', component: DeploymentComponent},
    {path: 'deployment/:appserverName', component: DeploymentComponent},
    {path: 'deployment/:appserverName/:releaseName', component: DeploymentComponent},
    {path: '**', component: PageNotFoundComponent},
  ])],
  exports: [RouterModule]
})
export class DeploymentRoutingModule {
}
