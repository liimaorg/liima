import { Component, computed, inject, linkedSignal, Signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { TileComponent } from '../../../shared/tile/tile.component';
import { LoadingIndicatorComponent } from '../../../shared/elements/loading-indicator.component';
import { ButtonComponent } from '../../../shared/button/button.component';
import { ResourceService } from '../../services/resource.service';
import { GroupedResourceRelations, ResourceRelation, UnresolvedRelation } from '../../models/resource-relation';
import { Resource } from '../../models/resource';
import { RelationGroupItem, RelationGroupComponent } from '../../relation-group/relation-group.component';
import { PropertiesPanelComponent } from '../../properties-panel/properties-panel.component';
import { PropertiesListComponent } from '../../properties-list/properties-list.component';
import { NgOptionComponent, NgSelectComponent } from '@ng-select/ng-select';
import { IconComponent } from '../../../shared/icon/icon.component';
import { Property } from '../../models/property';
import { BaseRelationsDirective } from '../../base-relations/base-relations.directive';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-resource-relations',
  standalone: true,
  imports: [
    TileComponent,
    LoadingIndicatorComponent,
    RelationGroupComponent,
    ButtonComponent,
    FormsModule,
    PropertiesPanelComponent,
    PropertiesListComponent,
    NgOptionComponent,
    NgSelectComponent,
    IconComponent,
    RouterLink,
  ],
  templateUrl: './resource-relations.component.html',
  styleUrl: './resource-relations.component.scss',
})
export class ResourceRelationsComponent extends BaseRelationsDirective {
  private resourceService = inject(ResourceService);
  resource: Signal<Resource> = this.resourceService.resource;

  protected groupedRelations: Signal<GroupedResourceRelations> = this.relationsService.relations;
  protected isLoadingRelations = this.relationsService.isLoadingRelations;

  protected hasRelations = computed(() => {
    const g = this.groupedRelations();
    return g.runtime.length + g.consumed.length + g.provided.length + g.unresolved.length > 0;
  });

  protected activeRelationId = linkedSignal(() => {
    const relId = this.selectedRelationId();
    if (relId != null && relId > 0) return relId;
    const g = this.groupedRelations();
    return [...g.runtime, ...g.consumed, ...g.provided][0]?.id ?? null;
  });

  selectedRelation = computed<ResourceRelation | null>(() => {
    const relId = this.activeRelationId();
    if (relId == null) return null;
    const g = this.groupedRelations();
    const all = [...g.runtime, ...g.consumed, ...g.provided];
    return all.find((r) => r.id === relId) ?? null;
  });

  selectedRelationIdForRelease = linkedSignal(() => this.selectedRelation()?.id ?? null);

  protected isLoadingProperties = this.relationsService.isLoadingRelationProperties;

  protected properties = computed<Property[]>(() => {
    const props = this.relationsService.relationProperties;
    const result: Property[] = [];
    if (this.hasIdentifierProperty()) {
      result.push(this.relationIdentifier());
    }
    result.push(...props());
    return result;
  });

  protected reloadRelation(entityId: number): void {
    this.relationsService.setIdForResourceRelations(entityId);
  }

  protected reloadProperties(entityId: number, relationId: number, contextId: number): void {
    this.relationsService.setIdsForRelationProperties(entityId, relationId, contextId);
  }

  protected getEntityId(): number {
    return this.resource().id;
  }

  protected getUnsavedChangesKey(): string {
    return 'resource-relation-properties';
  }

  protected getEditorOptions(): { includeResetsInHasChanges: boolean; unmarkResetOnChange: boolean } {
    return {
      includeResetsInHasChanges: true,
      unmarkResetOnChange: true,
    };
  }

  onReleaseChange(relationId: number) {
    this.activeRelationId.set(relationId);
    this.setQueryParamForRelationId(relationId);
  }

  protected hasIdentifierProperty() {
    const rel = this.selectedRelation();
    return rel != null && rel.relationType === 'consumed' && rel.type !== 'RUNTIME';
  }

  relationIdentifier = computed<Property>(() => ({
    name: 'relationName',
    displayName: `Relation name`,
    value: this.selectedRelation()?.identifier || '',
    replacedValue: '',
    generalComment: '',
    valueComment: 'specialProperty',
    descriptorId: -1,
    context: 'Global',
    nullable: true,
    optional: true,
    disabled: this.contextId() !== 1,
  }));

  protected toUnresolvedItem(unresolved: UnresolvedRelation): RelationGroupItem {
    return {
      key: `${unresolved.type}::${unresolved.name}`,
      name: unresolved.name,
      type: unresolved.type,
      unresolved: true,
    };
  }
}
