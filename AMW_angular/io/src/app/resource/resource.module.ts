import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ResourceListComponent } from './resource-list.component';
import { ResourceComponent } from './resource.component';
import { ResourceService } from './resource.service';
import { ResourceRoutingModule } from './resource-routing.module';

@NgModule({
  imports: [ CommonModule, ResourceRoutingModule ],
  declarations: [ ResourceListComponent, ResourceComponent ],
  providers: [ ResourceService ]
})
export class ResourceModule {}
