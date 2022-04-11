import { Routes, RouterModule } from '@angular/router';
import { NgModule } from '@angular/core';
import { DeploymentsComponent } from './deployments/deployments.component';

export const routes: Routes = [
  // default route only, the rest is done in module routing
  { path: '', component: DeploymentsComponent }
];

@NgModule({
  imports: [RouterModule.forRoot(routes, { useHash: true, relativeLinkResolution: 'legacy' })],
  exports: [RouterModule]
})
export class AppRoutingModule {}
