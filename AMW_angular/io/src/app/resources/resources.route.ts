import { ResourcesPageComponent } from './resources-page.component';
import { ResourceEditComponent } from './resource-edit/resource-edit.component';

export const resourcesRoute = [
  { path: 'resources', component: ResourcesPageComponent },
  { path: 'resource/edit/:id', component: ResourceEditComponent },
];
