import { RouterModule } from '@angular/router';
import { DeploymentsComponent } from './deployments.component';
import { NgModule } from '@angular/core';

@NgModule({
  imports: [
    RouterModule.forChild([
      { path: 'deployments', component: DeploymentsComponent }
    ])
  ],
  exports: [RouterModule]
})
export class DeploymentsRoutingModule {}
