import { Component, computed, inject, linkedSignal, Signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { Observable } from 'rxjs';
import { NgOptionComponent, NgSelectComponent } from '@ng-select/ng-select';
import { TileComponent } from '../../../shared/tile/tile.component';
import { LoadingIndicatorComponent } from '../../../shared/elements/loading-indicator.component';
import { ButtonComponent } from '../../../shared/button/button.component';
import { IconComponent } from '../../../shared/icon/icon.component';
import { ResourceService } from '../../services/resource.service';
import { PropertyUpdate } from '../../services/resource-properties.service';
import { GroupedResourceRelations, ResourceRelation, UnresolvedRelation } from '../../models/resource-relation';
import { Resource } from '../../models/resource';
import { Property } from '../../models/property';
import { RelationGroupItem, RelationGroupComponent } from '../../relation-group/relation-group.component';
import { PropertiesPanelComponent } from '../../properties-panel/properties-panel.component';
import { PropertiesListComponent } from '../../properties-list/properties-list.component';
import { BaseRelationsDirective, NODE_FILTERED_PROPERTIES } from '../../base-relations/base-relations.directive';

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

  protected permissions = computed(() => {
    if (this.authService.restrictions().length > 0) {
      return {
        canUpdateProperty: this.authService.hasPermission(
          'RESOURCE',
          'UPDATE',
          null,
          this.resource()?.resourceTypeId,
          this.context()?.name,
        ),
        canDecryptProperties: this.authService.hasPermission(
          'RESOURCE_PROPERTY_DECRYPT',
          'ALL',
          null,
          this.resource()?.id,
          this.context()?.name,
        ),
      };
    } else {
      return { canUpdateProperty: false, canDecryptProperties: false };
    }
  });

  protected hasRelations = computed(() => {
    const g = this.groupedRelations();
    return g.runtime.length + g.consumed.length + g.provided.length + g.unresolved.length > 0;
  });

  // set by url, input, onItemSelected and onReleaseChange
  protected activeRelationId = linkedSignal(() => {
    const relId = this.selectedRelationId();
    if (relId != null && relId > 0) return relId;
    const g = this.groupedRelations();
    return [...g.runtime, ...g.consumed, ...g.provided][0]?.id ?? null;
  });

  // needed for css highlighting on switching releases
  selectedItemKey = computed<number | null>(() => {
    const relId = this.activeRelationId();
    if (relId == null) return null;
    const g = this.groupedRelations();
    const all = [...g.runtime, ...g.consumed, ...g.provided];
    if (all.some((r) => r.id === relId)) return relId;
    for (const r of all) {
      if (r.availableReleases?.some((ar) => ar.relationId === relId)) {
        return r.id;
      }
    }
    return relId;
  });

  // resolves selected releation and handles releases fallback
  selectedRelation = computed<ResourceRelation | null>(() => {
    const relId = this.activeRelationId();
    if (relId == null) return null;
    const g = this.groupedRelations();
    const all = [...g.runtime, ...g.consumed, ...g.provided];
    const direct = all.find((r) => r.id === relId);
    if (direct) return direct;
    for (const r of all) {
      const release = r.availableReleases?.find((ar) => ar.relationId === relId);
      if (release) {
        return { ...r, id: relId, slaveId: release.slaveId, relatedResourceRelease: release.releaseName };
      }
    }
    return null;
  });

  selectedRelationIdForRelease = computed(() => this.selectedRelation()?.id ?? null);

  protected isLoadingProperties = this.relationsService.isLoadingRelationProperties;

  protected properties = computed<Property[]>(() => {
    const props = this.relationsService.relationProperties;
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

  protected entityId = computed(() => this.resource()?.id);

  protected reloadRelation(entityId: number): void {
    this.relationsService.setIdForResourceRelations(entityId);
  }

  protected reloadProperties(entityId: number, relationId: number, contextId: number): void {
    this.relationsService.setIdsForRelationProperties(entityId, relationId, contextId);
  }

  protected bulkUpdateProperties(
    relationId: number,
    updatedProperties: PropertyUpdate[],
    resetProperties: PropertyUpdate[],
    contextId: number,
  ): Observable<void> {
    return this.relationsService.bulkUpdateResourceRelationProperties(
      this.entityId(),
      relationId,
      updatedProperties,
      resetProperties,
      contextId,
    );
  }

  protected afterPropertiesSaved(): void {
    const changes = this.editor.changedProperties();
    if (changes.has('relationName')) {
      this.reloadRelation(this.entityId());
    }
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

  protected hasIdentifierProperty() {
    const rel = this.selectedRelation();
    return rel != null && rel.relationType === 'consumed' && rel.type !== 'RUNTIME';
  }

  protected toUnresolvedItem(unresolved: UnresolvedRelation): RelationGroupItem {
    return {
      key: `${unresolved.type}::${unresolved.name}`,
      name: unresolved.name,
      type: unresolved.type,
      unresolved: true,
    };
  }

  onReleaseChange(relationId: number) {
    this.activeRelationId.set(relationId);
    this.setQueryParamForRelationId(relationId);
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
}
