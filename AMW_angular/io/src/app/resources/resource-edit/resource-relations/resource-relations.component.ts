import { Component, computed, effect, inject, input, Signal } from '@angular/core';
import { TileComponent } from '../../../shared/tile/tile.component';
import { LoadingIndicatorComponent } from '../../../shared/elements/loading-indicator.component';
import { ResourceRelationsService } from '../../services/resource-relations.service';
import { ResourceService } from '../../services/resource.service';
import { GroupedResourceRelations } from '../../models/resource-relation';
import { Resource } from '../../models/resource';

@Component({
  selector: 'app-resource-relations',
  standalone: true,
  imports: [TileComponent, LoadingIndicatorComponent],
  templateUrl: './resource-relations.component.html',
  styleUrl: './resource-relations.component.scss',
})
export class ResourceRelationsComponent {
  private relationsService = inject(ResourceRelationsService);
  private resourceService = inject(ResourceService);

  contextId = input.required<number>();
  resource: Signal<Resource> = this.resourceService.resource;

  groupedRelations: Signal<GroupedResourceRelations> = this.relationsService.relations;
  isLoading = this.relationsService.isLoadingRelations;

  hasRelations = computed(() => {
    const g = this.groupedRelations();
    return g.runtime.length + g.consumed.length + g.provided.length + g.unresolved.length > 0;
  });

  constructor() {
    effect(() => {
      const resourceId = this.resource()?.id;
      if (resourceId) {
        this.relationsService.setIdForResourceRelations(resourceId);
      }
    });
  }
}
