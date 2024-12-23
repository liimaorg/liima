import { ResourcesPageComponent } from './resources-page.component';
import { ResourceEditPageComponent } from './resource-edit-page/resource-edit-page.component';
import { ResourceTypeEditPageComponent } from './resource-type-edit-page/resource-type-edit-page.component';

export const resourcesRoute = [
  { path: 'resources', component: ResourcesPageComponent },
  { path: 'resource/edit', component: ResourceEditPageComponent },
  { path: 'resourceType/edit', component: ResourceTypeEditPageComponent },
];
