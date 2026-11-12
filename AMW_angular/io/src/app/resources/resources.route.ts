import { ResourceEditComponent } from './resource-edit/resource-edit.component';
import { ResourcesPageComponent } from './resources-page.component';
import { ResourceTypeEditComponent } from './resource-type-edit/resource-type-edit.component';
import { TestGenerationComponent } from './test-generation/test-generation.component';
import { ResourceDependenciesComponent } from './resource-dependencies/resource-dependencies.component';

export const resourcesRoute = [
  { path: 'resources', component: ResourcesPageComponent, title: 'Resources - Liima' },
  { path: 'resource/edit', component: ResourceEditComponent },
  { path: 'resourceType/edit', component: ResourceTypeEditComponent },
  { path: 'resource/test-generation', component: TestGenerationComponent },
  { path: 'resource/dependencies', component: ResourceDependenciesComponent },
];
