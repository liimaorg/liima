import { Routes, RouterModule } from '@angular/router';
import { NgModule } from '@angular/core';
import { DeploymentsComponent } from './deployments/deployments.component';
import {TagsComponent} from "./settings/tags/tags.component";
import {SettingsComponent} from "./settings/settings.component";

export const routes: Routes = [
  // default route only, the rest is done in module routing
  { path: '', component: DeploymentsComponent },
  {
    path: 'settings',
    component: SettingsComponent,
    children: [
      { path: 'tags', component: TagsComponent },
    ],
  },
];

@NgModule({
  imports: [RouterModule.forRoot(routes, { useHash: true })],
  exports: [RouterModule]
})
export class AppRoutingModule {}
