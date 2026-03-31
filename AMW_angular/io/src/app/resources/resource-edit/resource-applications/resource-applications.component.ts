import { Component, computed, effect, inject, input, signal } from '@angular/core';
import { ResourceApplicationsService } from '../../services/resource-applications.service';
import { ApplicationRelation } from '../../models/application-relation';
import { AuthService } from '../../../auth/auth.service';
import { RouterLink } from '@angular/router';
import { TileComponent } from '../../../shared/tile/tile.component';
import { LoadingIndicatorComponent } from '../../../shared/elements/loading-indicator.component';
import { ButtonComponent } from '../../../shared/button/button.component';
import { Resource } from '../../models/resource';

@Component({
  selector: 'app-resource-applications',
  standalone: true,
  imports: [RouterLink, TileComponent, LoadingIndicatorComponent, ButtonComponent],
  templateUrl: './resource-applications.component.html',
  styleUrl: './resource-applications.component.scss',
})
export class ResourceApplicationsComponent {
  private resourceApplicationsService = inject(ResourceApplicationsService);
  private authService = inject(AuthService);

  resource = input.required<Resource>();
  contextId = input.required<number>();

  applications = signal<ApplicationRelation[]>([]);
  isLoading = signal<boolean>(false);
  error = signal<string | null>(null);

  permissions = computed(() => {
    if (this.authService.restrictions().length > 0) {
      return {
        canListRelations: this.authService.hasPermission('RESOURCE', 'READ'),
      };
    }
    return { canListRelations: false };
  });

  constructor() {
    effect(() => {
      const id = this.resource().id;
      if (!id) return;
      if (this.resource().type === '"APPLICATIONSERVER"') return;
      if (!this.permissions().canListRelations) return;

      this.loadApplications(id);
    });
  }

  private loadApplications(resourceId: number): void {
    this.isLoading.set(true);
    this.error.set(null);

    this.resourceApplicationsService.getApplicationsForResource(resourceId).subscribe({
      next: (applications) => {
        this.applications.set(applications);
        this.isLoading.set(false);
      },
      error: (err) => {
        console.error('Failed to load applications:', err);
        this.error.set('Failed to load applications');
        this.isLoading.set(false);
      },
    });
  }
}
