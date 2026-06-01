import { Component, computed, inject, linkedSignal, Signal } from '@angular/core';
import { TileComponent } from '../../../shared/tile/tile.component';
import { LoadingIndicatorComponent } from '../../../shared/elements/loading-indicator.component';
import { ResourceTypesService } from '../../services/resource-types.service';
import { GroupedResourceRelations, UnresolvedRelation } from '../../models/resource-relation';
import { ResourceType } from '../../models/resource-type';
import { RelationGroupItem, RelationGroupComponent } from '../../relation-group/relation-group.component';
import { ButtonComponent } from '../../../shared/button/button.component';
import { IconComponent } from '../../../shared/icon/icon.component';
import { PropertiesListComponent } from '../../properties-list/properties-list.component';
import { PropertiesPanelComponent } from '../../properties-panel/properties-panel.component';
import { Property } from '../../models/property';
import { BaseRelationsDirective, NODE_FILTERED_PROPERTIES } from '../../base-relations/base-relations.directive';

@Component({
  selector: 'app-resource-type-relations',
  standalone: true,
  imports: [
    TileComponent,
    LoadingIndicatorComponent,
    RelationGroupComponent,
    ButtonComponent,
    IconComponent,
    PropertiesListComponent,
    PropertiesPanelComponent,
  ],
  templateUrl: './resource-type-relations.component.html',
  styleUrl: './resource-type-relations.component.scss',
})
export class ResourceTypeRelationsComponent extends BaseRelationsDirective {
  private resourceTypeService = inject(ResourceTypesService);
  resourceType: Signal<ResourceType> = this.resourceTypeService.resourceType;

  protected groupedRelations: Signal<GroupedResourceRelations> = this.relationsService.typeRelations;
  protected isLoadingRelations = this.relationsService.isLoadingTypeRelations;

  protected permissions = computed(() => {
    if (this.authService.restrictions().length > 0) {
      return {
        canUpdateProperty: this.authService.hasPermission(
          'RESOURCETYPE',
          'UPDATE',
          this.resourceType()?.name,
          null,
          this.context()?.name,
        ),
        canDecryptProperties: this.authService.hasPermission(
          'RESOURCETYPE_PROPERTY_DECRYPT',
          'ALL',
          this.resourceType()?.name,
          null,
          this.context()?.name,
        ),
      };
    } else {
      return { canUpdateProperty: false, canDecryptProperties: false };
    }
  });

  // Resource types currently only have unresolved relations (no runtime/consumed/provided).
  protected hasRelations = computed(() => this.groupedRelations().unresolved.length > 0);

  protected activeRelationId = linkedSignal(() => {
    const id = this.selectedRelationId();
    if (id != null && id > 0) return id;
    return this.groupedRelations().unresolved[0]?.resRelTypeId ?? null;
  });

  selectedRelation = computed<UnresolvedRelation | null>(() => {
    const id = this.activeRelationId();
    if (id == null) return null;
    return this.groupedRelations().unresolved.find((r) => r.resRelTypeId === id) ?? null;
  });

  protected isLoadingProperties = this.relationsService.isLoadingTypeRelationProperties;

  protected properties = computed<Property[]>(() => {
    const props = this.relationsService.typeRelationProperties;
    const result: Property[] = [];
    if (this.hasIdentifierProperty()) {
      result.push(this.relationIdentifier());
    }
    const allProps = props();
    const rel = this.selectedRelation();
    if (rel?.type === 'NODE' && !this.isEnvironment()) {
      result.push(...allProps.filter((p) => !NODE_FILTERED_PROPERTIES.includes(p.name)));
    } else {
      result.push(...allProps);
    }
    return result;
  });

  protected reloadRelation(entityId: number): void {
    this.relationsService.setIdForResourceTypeRelations(entityId);
  }

  protected reloadProperties(entityId: number, relationId: number, contextId: number): void {
    this.relationsService.setIdsForTypeRelationProperties(entityId, relationId, contextId);
  }

  protected entityId = computed(() => this.resourceType()?.id);

  protected getUnsavedChangesKey(): string {
    return 'resourceType-relation-properties';
  }

  protected getEditorOptions(): { includeResetsInHasChanges: boolean; unmarkResetOnChange: boolean } {
    return {
      includeResetsInHasChanges: true,
      unmarkResetOnChange: true,
    };
  }

  protected hasIdentifierProperty() {
    return this.selectedRelation() != null;
  }

  relationIdentifier = computed<Property>(() => ({
    name: 'relationName',
    displayName: `Relation name`,
    value: this.selectedRelation()?.identifier || '',
    replacedValue: '',
    generalComment: '',
    valueComment: 'specialProperty',
    descriptorId: -1,
    context: 'Global', // TODO set context for resource type?
    nullable: true,
    optional: true,
    disabled: !this.permissions().canUpdateProperty,
  }));

  protected toUnresolvedItem(unresolved: UnresolvedRelation): RelationGroupItem {
    return {
      key: unresolved.resRelTypeId,
      name: unresolved.name,
      type: unresolved.type,
      unresolved: true,
      identifier: unresolved.identifier,
    };
  }
}
