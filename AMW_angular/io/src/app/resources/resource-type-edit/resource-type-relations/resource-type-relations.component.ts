import { Component, computed, effect, inject, input, linkedSignal, output, signal, Signal } from '@angular/core';
import { TileComponent } from '../../../shared/tile/tile.component';
import { LoadingIndicatorComponent } from '../../../shared/elements/loading-indicator.component';
import { ResourceRelationsService } from '../../services/resource-relations.service';
import { ResourceTypesService } from '../../services/resource-types.service';
import { GroupedResourceRelations, UnresolvedRelation } from '../../models/resource-relation';
import { Property } from '../../models/property';
import { ResourceType } from '../../models/resource-type';
import {
  RelationGroupItem,
  ResourceRelationGroupComponent,
} from '../../resource-edit/resource-relations/resource-relation-group/resource-relation-group.component';
import { ButtonComponent } from '../../../shared/button/button.component';
import { IconComponent } from '../../../shared/icon/icon.component';
import { PropertiesListComponent } from '../../properties-list/properties-list.component';
import { PropertiesPanelComponent } from '../../properties-panel/properties-panel.component';

@Component({
  selector: 'app-resource-type-relations',
  standalone: true,
  imports: [
    TileComponent,
    LoadingIndicatorComponent,
    ResourceRelationGroupComponent,
    ButtonComponent,
    IconComponent,
    PropertiesListComponent,
    PropertiesPanelComponent,
  ],
  templateUrl: './resource-type-relations.component.html',
  styleUrl: './resource-type-relations.component.scss',
})
export class ResourceTypeRelationsComponent {
  private relationsService = inject(ResourceRelationsService);
  private resourceTypeService = inject(ResourceTypesService);

  contextId = input.required<number>();
  resourceType: Signal<ResourceType> = this.resourceTypeService.resourceType;
  selectedRelationId = input<number | null>(null);
  relationSelected = output<number | null>();

  groupedRelations: Signal<GroupedResourceRelations> = this.relationsService.typeRelations;
  isLoading = this.relationsService.isLoadingTypeRelations;

  hasRelations = computed(() => this.groupedRelations().unresolved.length > 0);

  unresolvedItems = computed(() => this.groupedRelations().unresolved.map((u) => this.toUnresolvedItem(u)));

  activeRelationId = linkedSignal(() => {
    const id = this.selectedRelationId();
    if (id != null) return id;
    return this.groupedRelations().unresolved[0]?.resRelTypeId ?? null;
  });

  selectedRelation = computed<UnresolvedRelation | null>(() => {
    const id = this.activeRelationId();
    if (id == null) return null;
    return this.groupedRelations().unresolved.find((r) => r.resRelTypeId === id) ?? null;
  });

  properties = signal<Property[] | null>([]);

  constructor() {
    effect(() => {
      const resourceTypeId = this.resourceType()?.id;
      if (resourceTypeId) {
        this.relationsService.setIdForResourceTypeRelations(resourceTypeId);
      }
    });
  }

  onItemSelected(item: RelationGroupItem) {
    const id = typeof item.key === 'number' ? item.key : null;
    this.activeRelationId.set(id);
    this.relationSelected.emit(id);
  }

  private toUnresolvedItem(unresolved: UnresolvedRelation): RelationGroupItem {
    return {
      key: unresolved.resRelTypeId,
      name: unresolved.name,
      type: unresolved.type,
      unresolved: true,
    };
  }
}
