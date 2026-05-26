import { Component, computed, effect, inject, input, linkedSignal, output, signal, Signal } from '@angular/core';
import { TileComponent } from '../../../shared/tile/tile.component';
import { LoadingIndicatorComponent } from '../../../shared/elements/loading-indicator.component';
import { ResourceRelationsService } from '../../services/resource-relations.service';
import { ResourceTypesService } from '../../services/resource-types.service';
import { GroupedResourceRelations, UnresolvedRelation } from '../../models/resource-relation';
import { ResourceType } from '../../models/resource-type';
import { RelationGroupItem, RelationGroupComponent } from '../../relation-group/relation-group.component';
import { ButtonComponent } from '../../../shared/button/button.component';
import { IconComponent } from '../../../shared/icon/icon.component';
import { PropertiesListComponent } from '../../properties-list/properties-list.component';
import { PropertiesPanelComponent } from '../../properties-panel/properties-panel.component';
import { createPropertiesEditor } from '../../properties-editor';
import { Property } from '../../models/property';
import { AuthService } from '../../../auth/auth.service';

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
export class ResourceTypeRelationsComponent {
  private authService = inject(AuthService);
  private relationsService = inject(ResourceRelationsService);
  private resourceTypeService = inject(ResourceTypesService);

  contextId = input.required<number>();
  resourceType: Signal<ResourceType> = this.resourceTypeService.resourceType;
  selectedRelationId = input<number | null>(null);
  relationSelected = output<number | null>();

  groupedRelations: Signal<GroupedResourceRelations> = this.relationsService.typeRelations;
  isLoading = this.relationsService.isLoadingTypeRelations;
  permissions = computed(() => {
    if (this.authService.restrictions().length > 0) {
      return {
        canEditResourceType: this.authService.hasPermission('RESOURCETYPE', 'UPDATE', this.resourceType().name),
      };
    } else {
      return { canEditResourceType: false };
    }
  });
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

  properties = computed<Property[]>(() => {
    const props = this.relationsService.typeRelationProperties;
    const result: Property[] = [];
    if (this.hasIdentifierProperty()) {
      result.push(this.relationIdentifier());
    }
    result.push(...props());
    return result;
  });

  isLoadingProperties = this.relationsService.isLoadingTypeRelationProperties;

  protected invalidProperties = signal<Set<string>>(new Set());

  protected editor = createPropertiesEditor(() => [...this.properties().filter((p) => !p.disabled)], {
    includeResetsInHasChanges: true,
    unmarkResetOnChange: true,
  });

  resetToken = this.editor.resetToken;

  constructor() {
    effect(() => {
      const resourceTypeId = this.resourceType()?.id;
      if (resourceTypeId) {
        this.relationsService.setIdForResourceTypeRelations(resourceTypeId);
      }
    });

    effect(() => {
      const relTypeId = this.activeRelationId();
      const resourceTypeId = this.resourceType()?.id;
      const ctxId = this.contextId();
      if (relTypeId != null && resourceTypeId != null && ctxId != null) {
        this.relationsService.setIdsForTypeRelationProperties(resourceTypeId, relTypeId, ctxId);
      }
    });
  }

  onPropertyReset(propertyName: string, checked: boolean) {
    this.editor.onPropertyReset(propertyName, checked);
  }

  onPropertyValidationChange(propertyName: string, invalid: boolean) {
    this.invalidProperties.update((set) => {
      const next = new Set(set);
      if (invalid) {
        next.add(propertyName);
      } else {
        next.delete(propertyName);
      }
      return next;
    });
  }

  onItemSelected(item: RelationGroupItem) {
    const id = typeof item.key === 'number' ? item.key : null;
    this.activeRelationId.set(id);
    this.relationSelected.emit(id);
  }

  private hasIdentifierProperty() {
    return this.selectedRelation() != null && !this.resourceType().isDefaultResourceType;
  }

  private relationIdentifier = computed<Property>(() => ({
    name: 'relationName',
    displayName: `Relation name`,
    value: this.selectedRelation()?.identifier || '',
    replacedValue: '',
    generalComment: '',
    valueComment: 'specialProperty',
    descriptorId: -1,
    context: 'Global', // TODO fix context
    nullable: true,
    optional: true,
    disabled: this.permissions().canEditResourceType,
  }));

  private toUnresolvedItem(unresolved: UnresolvedRelation): RelationGroupItem {
    return {
      key: unresolved.resRelTypeId,
      name: unresolved.name,
      type: unresolved.type,
      unresolved: true,
      identifier: unresolved.identifier,
    };
  }
}
