import { Component, computed, inject, Signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { toSignal } from '@angular/core/rxjs-interop';
import { map, switchMap } from 'rxjs/operators';
import { ResourceDependenciesService } from '../services/resource-dependencies.service';
import { ResourceDependencies, ResourceDependency } from '../models/resource-dependency';
import { LoadingIndicatorComponent } from '../../shared/elements/loading-indicator.component';
import { PageComponent } from '../../layout/page/page.component';
import { IconComponent } from '../../shared/icon/icon.component';
import { TableColumnType, TableComponent } from '../../shared/table/table.component';
import { AuthService } from '../../auth/auth.service';

@Component({
  selector: 'app-resource-dependencies',
  standalone: true,
  imports: [LoadingIndicatorComponent, PageComponent, IconComponent, TableComponent, RouterLink],
  templateUrl: './resource-dependencies.component.html',
})
export class ResourceDependenciesComponent {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private dependenciesService = inject(ResourceDependenciesService);
  private authService = inject(AuthService);

  permissions = computed(() => {
    if (this.authService.restrictions().length > 0) {
      return {
        canReadResources: this.authService.hasPermission('RESOURCE', 'READ'),
        // TODO: remove after migration
        canUseAngularEditResource:
          this.authService.hasPermission('RESOURCE', 'READ') &&
          this.authService.hasPermission('ANGULAR_EDIT_RESOURCE', 'ALL'),
      };
    } else {
      return {
        canReadResources: false,
        canUseAngularEditResource: false,
      };
    }
  });

  resourceId = toSignal(this.route.queryParamMap.pipe(map((params) => Number(params.get('id')))), {
    initialValue: 0,
  });

  contextId = toSignal(this.route.queryParamMap.pipe(map((params) => Number(params.get('ctx')))), {
    initialValue: 1,
  });

  dependencies: Signal<ResourceDependencies | undefined> = toSignal(
    this.route.queryParamMap.pipe(
      map((params) => Number(params.get('id'))),
      switchMap((id) => this.dependenciesService.getResourceDependencies(id)),
    ),
  );

  isLoading = computed(() => !this.dependencies());

  hasConsumedRelations = computed(() => (this.dependencies()?.consumedRelations?.length ?? 0) > 0);

  hasProvidedRelations = computed(() => (this.dependencies()?.providedRelations?.length ?? 0) > 0);

  hasDependencies = computed(() => this.hasConsumedRelations() || this.hasProvidedRelations());

  consumedRelationsTableData = computed(() =>
    (this.dependencies()?.consumedRelations ?? []).map((dep) => ({ ...dep, id: dep.resourceId })),
  );

  providedRelationsTableData = computed(() =>
    (this.dependencies()?.providedRelations ?? []).map((dep) => ({ ...dep, id: dep.resourceId })),
  );

  dependenciesHeader(): TableColumnType<ResourceDependency>[] {
    return [
      {
        key: 'resourceTypeName',
        columnTitle: 'Resource Type',
      },
      {
        key: 'resourceName',
        columnTitle: 'Resource Name',
      },
      {
        key: 'releaseName',
        columnTitle: 'Release',
      },
    ];
  }

  navigateToResource(id: number): void {
    this.router.navigate(['/resource/edit'], { queryParams: { id, ctx: 1 } });
  }

  openEditResourcePage(id: number) {
    window.location.href = `/AMW_web/pages/editResourceView.xhtml?ctx=1&id=${id}`;
  }
}
