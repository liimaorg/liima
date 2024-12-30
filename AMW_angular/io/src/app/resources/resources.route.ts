import { ResourceEditComponent } from './resource-edit/resource-edit.component';
import { ResourcesPageComponent } from './resources-page.component';
import { ResourceTypeEditComponent } from './resource-type-edit/resource-type-edit.component';

export const resourcesRoute = [
  { path: 'resources', component: ResourcesPageComponent },
  { path: 'resource/edit', component: ResourceEditComponent },
  { path: 'resourceType/edit', component: ResourceTypeEditComponent },
];
