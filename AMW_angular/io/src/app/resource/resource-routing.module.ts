import { RouterModule } from '@angular/router';
import { ResourceListComponent } from './resource-list.component';
import { NgModule } from '@angular/core';

@NgModule({
  imports: [RouterModule.forChild([
    { path: 'resource', component: ResourceListComponent }
  ])],
  exports: [RouterModule]
})
export class ResourceRoutingModule {}
