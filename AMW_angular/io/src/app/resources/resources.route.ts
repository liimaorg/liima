import { ResourceEditComponent } from './resource-edit/resource-edit.component';
import { ResourcesPageComponent } from './resources-page.component';
import { ResourceTypeEditComponent } from './resource-type-edit/resource-type-edit.component';
import { TestGenerationComponent } from './test-generation/test-generation.component';
import { ResourceDependenciesComponent } from './resource-dependencies/resource-dependencies.component';

export const resourcesRoute = [
  { path: 'resources', component: ResourcesPageComponent, title: 'Resources - Liima' },
  { path: 'resource/edit', component: ResourceEditComponent, title: 'Edit Resource - Liima' },
  { path: 'resourceType/edit', component: ResourceTypeEditComponent, title: 'Edit Resource Type - Liima' },
  { path: 'resource/test-generation', component: TestGenerationComponent, title: 'Test Generation - Liima' },
  { path: 'resource/dependencies', component: ResourceDependenciesComponent, title: 'Resource Dependencies - Liima' },
];
