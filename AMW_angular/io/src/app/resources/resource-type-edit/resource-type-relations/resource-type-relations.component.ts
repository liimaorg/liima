import { Component, computed, effect, inject, input, Signal } from '@angular/core';
import { TileComponent } from '../../../shared/tile/tile.component';
import { LoadingIndicatorComponent } from '../../../shared/elements/loading-indicator.component';
import { ResourceRelationsService } from '../../services/resource-relations.service';
import { ResourceTypesService } from '../../services/resource-types.service';
import { GroupedResourceRelations, UnresolvedRelation } from '../../models/resource-relation';
import { ResourceType } from '../../models/resource-type';
import {
  RelationGroupItem,
  ResourceRelationGroupComponent,
} from '../../resource-edit/resource-relations/resource-relation-group/resource-relation-group.component';

@Component({
  selector: 'app-resource-type-relations',
  standalone: true,
  imports: [TileComponent, LoadingIndicatorComponent, ResourceRelationGroupComponent],
  templateUrl: './resource-type-relations.component.html',
  styleUrl: './resource-type-relations.component.scss',
})
export class ResourceTypeRelationsComponent {
  private relationsService = inject(ResourceRelationsService);
  private resourceTypeService = inject(ResourceTypesService);

  contextId = input.required<number>();
  resourceType: Signal<ResourceType> = this.resourceTypeService.resourceType;

  groupedRelations: Signal<GroupedResourceRelations> = this.relationsService.typeRelations;
  isLoading = this.relationsService.isLoadingTypeRelations;

  hasRelations = computed(() => this.groupedRelations().unresolved.length > 0);

  unresolvedItems = computed(() => this.groupedRelations().unresolved.map((u) => this.toUnresolvedItem(u)));

  constructor() {
    effect(() => {
      const resourceTypeId = this.resourceType()?.id;
      if (resourceTypeId) {
        this.relationsService.setIdForResourceTypeRelations(resourceTypeId);
      }
    });
  }

  private toUnresolvedItem(unresolved: UnresolvedRelation): RelationGroupItem {
    return {
      key: `${unresolved.type}::${unresolved.name}`,
      name: unresolved.name,
      type: unresolved.type,
      unresolved: true,
    };
  }
}
