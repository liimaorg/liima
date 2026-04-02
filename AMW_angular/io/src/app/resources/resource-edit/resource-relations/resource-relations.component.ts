import { Component, computed, effect, inject, input, Signal } from '@angular/core';
import { TileComponent } from '../../../shared/tile/tile.component';
import { LoadingIndicatorComponent } from '../../../shared/elements/loading-indicator.component';
import { ResourceRelationsService } from '../../services/resource-relations.service';
import { ResourceService } from '../../services/resource.service';
import { GroupedRelations, ResourceRelation } from '../../models/resource-relation';
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

  relations: Signal<ResourceRelation[]> = this.relationsService.relations;
  isLoading = this.relationsService.isLoadingRelations;

  groupedRelations = computed<GroupedRelations>(() => {
    const allRelations = this.relations();
    return {
      consumed: allRelations.filter((r) => r.relationType === 'consumed'),
      provided: allRelations.filter((r) => r.relationType === 'provided'),
    };
  });

  hasRelations = computed(() => this.relations().length > 0);

  constructor() {
    effect(() => {
      const resourceId = this.resource()?.id;
      if (resourceId) {
        this.relationsService.setIdForResourceRelations(resourceId);
      }
    });
  }
}
