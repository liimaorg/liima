import { Component, computed, effect, inject, input, Signal } from '@angular/core';
import { TileComponent } from '../../../shared/tile/tile.component';
import { LoadingIndicatorComponent } from '../../../shared/elements/loading-indicator.component';
import { ResourceRelationsService } from '../../services/resource-relations.service';
import { ResourceService } from '../../services/resource.service';
import { GroupedResourceRelations, ResourceRelation, UnresolvedRelation } from '../../models/resource-relation';
import { Resource } from '../../models/resource';
import {
  RelationGroupItem,
  ResourceRelationGroupComponent,
} from './resource-relation-group/resource-relation-group.component';

@Component({
  selector: 'app-resource-relations',
  standalone: true,
  imports: [TileComponent, LoadingIndicatorComponent, ResourceRelationGroupComponent],
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

  runtimeItems = computed(() => this.groupedRelations().runtime.map((r) => this.toItem(r)));
  consumedItems = computed(() => this.groupedRelations().consumed.map((r) => this.toItem(r)));
  providedItems = computed(() => this.groupedRelations().provided.map((r) => this.toItem(r)));
  unresolvedItems = computed(() => this.groupedRelations().unresolved.map((u) => this.toUnresolvedItem(u)));

  constructor() {
    effect(() => {
      const resourceId = this.resource()?.id;
      if (resourceId) {
        this.relationsService.setIdForResourceRelations(resourceId);
      }
    });
  }

  private toItem(relation: ResourceRelation): RelationGroupItem {
    return {
      key: relation.id,
      name: relation.relatedResourceName,
      type: relation.type,
      release: relation.relatedResourceRelease,
      identifier:
        relation.relationName && relation.relationName !== relation.relatedResourceName
          ? relation.relationName
          : undefined,
    };
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
