import { ResourcesPageComponent } from './resources-page.component';
import { ResourceEditPageComponent } from './resource-edit/resource-edit-page.component';

export const resourcesRoute = [
  { path: 'resources', component: ResourcesPageComponent },
  { path: 'resource/edit/:id', component: ResourceEditPageComponent },
];
