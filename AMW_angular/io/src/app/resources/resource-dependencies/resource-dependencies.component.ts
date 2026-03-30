import { Component, computed, inject, Signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { toSignal } from '@angular/core/rxjs-interop';
import { map, switchMap } from 'rxjs/operators';
import { ResourceDependenciesService } from '../services/resource-dependencies.service';
import { ResourceDependencies } from '../models/resource-dependency';
import { LoadingIndicatorComponent } from '../../shared/elements/loading-indicator.component';
import { PageComponent } from '../../layout/page/page.component';
import { ButtonComponent } from '../../shared/button/button.component';
import { DependenciesTableComponent } from './dependencies-table/dependencies-table.component';

@Component({
  selector: 'app-resource-dependencies',
  standalone: true,
  imports: [LoadingIndicatorComponent, PageComponent, ButtonComponent, DependenciesTableComponent],
  templateUrl: './resource-dependencies.component.html',
  styleUrl: './resource-dependencies.component.scss',
})
export class ResourceDependenciesComponent {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private dependenciesService = inject(ResourceDependenciesService);

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

  goBackToEdit() {
    void this.router.navigate(['/resource/edit'], {
      queryParams: { id: this.resourceId(), ctx: this.contextId() },
    });
  }
}
