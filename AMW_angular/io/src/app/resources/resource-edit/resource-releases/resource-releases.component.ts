import { Component, computed, inject, input, signal } from '@angular/core';
import { LoadingIndicatorComponent } from '../../../shared/elements/loading-indicator.component';
import { AuthService } from '../../../auth/auth.service';
import { TileComponent } from '../../../shared/tile/tile.component';
import { Release } from '../../models/release';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-resource-releases',
  standalone: true,
  imports: [LoadingIndicatorComponent, TileComponent, RouterLink],
  templateUrl: './resource-releases.component.html',
})
export class ResourceReleasesComponent {
  private authService = inject(AuthService);

  isLoading = signal(false);

  id = input.required<number>();
  releases = input.required<Release[]>();
  contextId = input.required<number>();
  resourceTypeId = input.required<number>();

  // same permissions for crud
  permissions = computed(() => {
    if (this.authService.restrictions().length > 0) {
      return {
        //canAddRelease: this.authService.hasPermission('RESOURCE', 'UPDATE', null, null, this.context().name),
        canAddRelease: true, // TODO: replace with actual permission check
      };
    }
    return { canAddRelease: false };
  });

  addRelease() {
    console.log('add release');
  }
}
