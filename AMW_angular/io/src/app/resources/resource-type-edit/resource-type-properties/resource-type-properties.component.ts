import { Component, computed, inject, Signal } from '@angular/core';
import { Property } from '../../models/property';
import { LoadingIndicatorComponent } from '../../../shared/elements/loading-indicator.component';
import { ResourceTypesService } from '../../services/resource-types.service';
import { ResourceType } from '../../models/resource-type';
import { PropertyUpdate } from '../../services/resource-properties.service';
import { TileComponent } from '../../../shared/tile/tile.component';
import { PropertiesListComponent } from '../../properties-list/properties-list.component';
import { PropertiesPanelComponent } from '../../properties-panel/properties-panel.component';
import { PropertyDeleteModalService } from '../../services/property-delete-modal.service';
import { PropertyDeleteModalComponent } from '../../property-delete-modal/property-delete-modal.component';
import { BasePropertiesComponent } from '../../base-properties/base-properties.component';

@Component({
  selector: 'app-resource-type-properties',
  standalone: true,
  imports: [
    LoadingIndicatorComponent,
    TileComponent,
    PropertiesListComponent,
    PropertiesPanelComponent,
    PropertyDeleteModalComponent,
  ],
  providers: [PropertyDeleteModalService],
  templateUrl: './resource-type-properties.component.html',
  styleUrl: './resource-type-properties.component.scss',
})
export class ResourceTypePropertiesComponent extends BasePropertiesComponent {
  private resourceTypeService = inject(ResourceTypesService);
  resourceType: Signal<ResourceType> = this.resourceTypeService.resourceType;

  properties = computed<Property[]>(() => {
    const props = this.propertiesService.propertiesForType;
    const result: Property[] = [];
    if (this.showResourceTypeNameProperty()) {
      result.push(this.resourceTypeNameProperty());
    }
    result.push(...props());
    return result;
  });

  isLoading = this.propertiesService.isLoadingResourceTypeProperties;

  permissions = computed(() => {
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

  protected getEntityId(): number {
    return this.resourceType()?.id;
  }

  protected getUnsavedChangesKey(): string {
    return 'resource-type-properties';
  }

  protected getEditorOptions() {
    return {
      includeResetsInHasChanges: false,
      unmarkResetOnChange: false,
    };
  }

  protected bulkUpdateProperties(
    entityId: number,
    updatedProperties: PropertyUpdate[],
    resetProperties: PropertyUpdate[],
    contextId: number,
  ) {
    return this.propertiesService.bulkUpdateResourceTypePropertiesValues(
      entityId,
      updatedProperties,
      resetProperties,
      contextId,
    );
  }

  protected reloadProperties(entityId: number, contextId: number): void {
    this.propertiesService.setIdsForResourceTypeProperties(entityId, contextId);
  }

  protected getDeleteParams(): [number | undefined, number | undefined] {
    return [undefined, this.resourceType().id];
  }

  protected getSaveDescriptorParams(): [number | undefined, number | undefined] {
    return [undefined, this.resourceType().id];
  }

  private resourceTypeNameProperty = computed<Property>(() => ({
    name: 'resourceTypeName',
    displayName: 'ResourceType name',
    value: this.resourceType()?.name || '',
    replacedValue: '',
    generalComment: '',
    valueComment: 'specialProperty',
    descriptorId: -1,
    context: 'Global',
    nullable: false,
    optional: false,
  }));

  private showResourceTypeNameProperty = computed(() => {
    return this.contextId() === 1;
  });
}
