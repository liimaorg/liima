import { RouterModule } from '@angular/router';
import { ResourceListComponent } from './resource-list.component';
import { NgModule } from '@angular/core';

@NgModule({
  imports: [RouterModule.forChild([
    { path: 'resource', component: ResourceListComponent },
    { path: 'resource/type/:type', component: ResourceListComponent },
    { path: 'resource/:resource', component: ResourceListComponent },
    { path: 'resource/:resource/:release', component: ResourceListComponent },
  ])],
  exports: [RouterModule]
})
export class ResourceRoutingModule {}
