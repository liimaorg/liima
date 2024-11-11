import { ChangeDetectionStrategy, Component, computed, inject, Signal, signal } from '@angular/core';
import { AuthService } from '../auth/auth.service';
import { PageComponent } from '../layout/page/page.component';
import { LoadingIndicatorComponent } from '../shared/elements/loading-indicator.component';
import { ResourceTypesService } from './resource-types.service';
import { ResourceType } from '../resource/resource-type';

@Component({
  selector: 'app-resources-page',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [PageComponent, LoadingIndicatorComponent],
  templateUrl: './resources-page.component.html',
})
export class ResourcesPageComponent {
  private authService = inject(AuthService);
  private resourceTypesService = inject(ResourceTypesService);

  predefinedResourceTypes: Signal<ResourceType[]> = this.resourceTypesService.predefinedResourceTypes;
  rootResourceTypes: Signal<ResourceType[]> = this.resourceTypesService.rootResourceTypes;
  isLoading = signal(false);

  permissions = computed(() => {
    if (this.authService.restrictions().length > 0) {
      return {
        canViewResourceTypes: this.authService.hasPermission('RES_TYPE_LIST_TAB', 'ALL'),
      };
    } else {
      return { canViewResourceTypes: false };
    }
  });
}
