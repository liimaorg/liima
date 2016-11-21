import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';

import {ResourceListComponent} from './resource-list.component';
import {ResourceComponent} from './resource.component';
import {ResourceService} from './resource.service';

@NgModule({
  imports: [ CommonModule ],
  declarations: [ ResourceListComponent, ResourceComponent ],
  exports: [ ResourceListComponent, ResourceComponent ],
  providers: [ ResourceService ]
})
export class ResourceModule {}
