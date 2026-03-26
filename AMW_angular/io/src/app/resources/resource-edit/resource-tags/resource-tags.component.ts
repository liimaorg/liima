import { Component, computed, inject, input, signal } from '@angular/core';
import { LoadingIndicatorComponent } from '../../../shared/elements/loading-indicator.component';
import { TileComponent } from '../../../shared/tile/tile.component';
import { AuthService } from '../../../auth/auth.service';
import { Resource } from '../../models/resource';

@Component({
  selector: 'app-resource-tags',
  templateUrl: './resource-tags.component.html',
  standalone: true,
  imports: [LoadingIndicatorComponent, TileComponent],
})
export class ResourceTagsComponent {
  private authService = inject(AuthService);

  resource = input.required<Resource>();

  protected readonly isLoading = signal<boolean>(false);
  protected readonly isApplicationServer = signal<boolean>(true);

  protected readonly permissions = computed(() => {
    if (this.authService.restrictions().length > 0) {
      const resourceTypeName = this.resource()?.type ?? null;
      const resourceGroupId = this.resource()?.resourceGroupId ?? null;
      return {
        canTagCurrentState: this.authService.hasPermission('RESOURCE', 'UPDATE', resourceTypeName, resourceGroupId),
      };
    } else {
      return { canTagCurrentState: false };
    }
  });

  protected tagCurrentState() {
    console.log('todo');
  }
}
