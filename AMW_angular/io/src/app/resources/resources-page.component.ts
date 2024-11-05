import { ChangeDetectionStrategy, Component, computed, inject, Signal, signal } from '@angular/core';
import { AuthService } from '../auth/auth.service';
import { PageComponent } from '../layout/page/page.component';
import { LoadingIndicatorComponent } from '../shared/elements/loading-indicator.component';
import { ResourceType } from './resourceType';
import { ResourceTypesService } from './resource-types.service';

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

  predefinedResources: Signal<ResourceType[]> = this.resourceTypesService.predefinedResources;
  rootResources: Signal<ResourceType[]> = this.resourceTypesService.rootResources;

  isLoading = signal(false);

  loadingPermissions = computed(() => {
    if (this.authService.restrictions().length > 0) {
      this.getUserPermissions();
    } else {
      return `<div>Could not load permissions</div>`;
    }
  });

  private getUserPermissions() {}
}
