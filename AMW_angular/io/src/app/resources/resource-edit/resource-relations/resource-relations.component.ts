import { Component, computed, effect, inject, input, linkedSignal, output, signal, Signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { TileComponent } from '../../../shared/tile/tile.component';
import { LoadingIndicatorComponent } from '../../../shared/elements/loading-indicator.component';
import { ButtonComponent } from '../../../shared/button/button.component';
import { ResourceRelationsService } from '../../services/resource-relations.service';
import { ResourceService } from '../../services/resource.service';
import { GroupedResourceRelations, ResourceRelation, UnresolvedRelation } from '../../models/resource-relation';
import { Resource } from '../../models/resource';
import {
  RelationGroupItem,
  ResourceRelationGroupComponent,
} from './resource-relation-group/resource-relation-group.component';
import { PropertiesPanelComponent } from '../../properties-panel/properties-panel.component';
import { PropertiesListComponent } from '../../properties-list/properties-list.component';
import { NgOptionComponent, NgSelectComponent } from '@ng-select/ng-select';
import { IconComponent } from '../../../shared/icon/icon.component';
import { Property } from '../../models/property';

@Component({
  selector: 'app-resource-relations',
  standalone: true,
  imports: [
    TileComponent,
    LoadingIndicatorComponent,
    ResourceRelationGroupComponent,
    ButtonComponent,
    FormsModule,
    PropertiesPanelComponent,
    PropertiesListComponent,
    NgOptionComponent,
    NgSelectComponent,
    IconComponent,
  ],
  templateUrl: './resource-relations.component.html',
  styleUrl: './resource-relations.component.scss',
})
export class ResourceRelationsComponent {
  private relationsService = inject(ResourceRelationsService);
  private resourceService = inject(ResourceService);

  contextId = input.required<number>();
  selectedRelationId = input<number | null>(null);
  relationSelected = output<number | null>();
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

  activeRelationId = linkedSignal(() => this.selectedRelationId());

  selectedRelation = computed<ResourceRelation | null>(() => {
    const relId = this.activeRelationId();
    if (relId == null) return null;
    const g = this.groupedRelations();
    const all = [...g.runtime, ...g.consumed, ...g.provided];
    return all.find((r) => r.id === relId) ?? null;
  });

  selectedReleaseId = linkedSignal(() => this.selectedRelation()?.id ?? null);
  protected readonly properties = signal<Property[] | null>([]);

  constructor() {
    effect(() => {
      const resourceId = this.resource()?.id;
      if (resourceId) {
        this.relationsService.setIdForResourceRelations(resourceId);
      }
    });
  }

  onItemSelected(item: RelationGroupItem) {
    const id = typeof item.key === 'number' ? item.key : null;
    this.activeRelationId.set(id);
    this.relationSelected.emit(id);
  }

  onReleaseChange(relationId: number) {
    this.activeRelationId.set(relationId);
    this.relationSelected.emit(relationId);
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
