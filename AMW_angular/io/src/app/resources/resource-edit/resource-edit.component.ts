import { Component, computed, inject, signal, Signal, OnInit } from '@angular/core';
import { LoadingIndicatorComponent } from '../../shared/elements/loading-indicator.component';
import { PageComponent } from '../../layout/page/page.component';
import { ActivatedRoute } from '@angular/router';
import { map } from 'rxjs/operators';
import { toSignal } from '@angular/core/rxjs-interop';
import { ResourceService } from '../../resource/resource.service';
import { Resource } from '../../resource/resource';
import { TileComponent } from '../../shared/tile/tile.component';
import { AuthService } from '../../auth/auth.service';
import { ResourceFunctionsListComponent } from './resource-functions/resource-functions-list.component';
import { ResourceTemplatesListComponent } from './resource-templates/resource-templates-list.component';
import { Release } from '../../resource/release';

@Component({
  selector: 'app-resource-edit',
  standalone: true,
  imports: [LoadingIndicatorComponent, PageComponent, ResourceFunctionsListComponent, ResourceTemplatesListComponent],
  templateUrl: './resource-edit.component.html',
})
export class ResourceEditComponent implements OnInit {
  private authService = inject(AuthService);
  private resourceService = inject(ResourceService);
  private route = inject(ActivatedRoute);

  id = toSignal(this.route.queryParamMap.pipe(map((params) => Number(params.get('id')))), { initialValue: 0 });
  contextId = toSignal(this.route.queryParamMap.pipe(map((params) => Number(params.get('ctx')))), { initialValue: 1 });
  resource: Signal<Resource> = this.resourceService.resource;

  releases = signal<Release[]>([]);

  ngOnInit(): void {
    console.log('resource', this.resource());
    this.resourceService.setIdForResource(this.id());
    this.loadReleases(this.resource()?.resourceGroupId);
  }

  isLoading = computed(() => {
    if (this.id()) {
      this.resourceService.setIdForResource(this.id());
      return false;
    } else return false;
  });

  permissions = computed(() => {
    if (this.authService.restrictions().length > 0) {
      return {
        canEditResource: this.authService.hasPermission('RESOURCE', 'READ'),
      };
    } else {
      return { canEditResource: false };
    }
  });

  loadReleases(resourceGroupId: number): void {
    console.log('load releases');
    this.resourceService.getReleasesForResourceGroup(resourceGroupId).subscribe((releaseMap) => {
      this.releases.set(releaseMap);
      console.log('Loaded Releases:', this.releases());
    });
  }
}
