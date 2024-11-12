import { ChangeDetectionStrategy, Component, computed, inject, Signal, signal } from '@angular/core';
import { AuthService } from '../auth/auth.service';
import { PageComponent } from '../layout/page/page.component';
import { LoadingIndicatorComponent } from '../shared/elements/loading-indicator.component';
import { ResourceTypesService } from './resource-types.service';
import { ResourceType } from '../resource/resource-type';
import { ResourcesListComponent } from './resources-list/resources-list.component';
import { ResourceService } from '../resource/resource.service';
import { Resource } from '../resource/resource';

@Component({
  selector: 'app-resources-page',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [PageComponent, LoadingIndicatorComponent, ResourcesListComponent],
  templateUrl: './resources-page.component.html',
})
export class ResourcesPageComponent {
  private authService = inject(AuthService);
  private resourceTypesService = inject(ResourceTypesService);
  private resourceService = inject(ResourceService);

  predefinedResourceTypes: Signal<ResourceType[]> = this.resourceTypesService.predefinedResourceTypes;
  resourceGroupListForTypeSignal: Signal<Resource[]> = this.resourceService.resourceGroupListForTypeSignal;
  rootResourceTypes: Signal<ResourceType[]> = this.resourceTypesService.rootResourceTypes;
  isLoading = signal(false);
  expandedResourceTypeId: number | null = null;

  permissions = computed(() => {
    if (this.authService.restrictions().length > 0) {
      return {
        canViewResourceTypes: this.authService.hasPermission('RES_TYPE_LIST_TAB', 'ALL'),
      };
    } else {
      return { canViewResourceTypes: false };
    }
  });

  toggleChildrenOrLoadResourcesList(resourceType: ResourceType): void {
    if (!resourceType.hasChildren) this.loadResourcesList(resourceType);
    if (resourceType.hasChildren)
      this.expandedResourceTypeId = this.expandedResourceTypeId === resourceType.id ? null : resourceType.id;
  }

  loadResourcesList(resourceType: ResourceType) {
    this.resourceService.setTypeForResourceGroupList(resourceType);
  }
}
