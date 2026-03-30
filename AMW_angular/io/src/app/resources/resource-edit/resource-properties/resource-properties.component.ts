import { Component, computed, inject, Signal } from '@angular/core';
import { Resource } from '../../models/resource';
import { Property } from '../../models/property';
import { ResourceService } from '../../services/resource.service';
import { LoadingIndicatorComponent } from '../../../shared/elements/loading-indicator.component';
import { PropertyUpdate } from '../../services/resource-properties.service';
import { TileComponent } from '../../../shared/tile/tile.component';
import { PropertiesPanelComponent } from '../../properties-panel/properties-panel.component';
import { PropertiesListComponent } from '../../properties-list/properties-list.component';
import { PropertyDeleteModalService } from '../../services/property-delete-modal.service';
import { PropertyDeleteModalComponent } from '../../property-delete-modal/property-delete-modal.component';
import { BasePropertiesComponent } from '../../base-properties/base-properties.component';

@Component({
  selector: 'app-resource-properties',
  standalone: true,
  imports: [
    LoadingIndicatorComponent,
    TileComponent,
    PropertiesPanelComponent,
    PropertiesListComponent,
    PropertyDeleteModalComponent,
  ],
  providers: [PropertyDeleteModalService],
  templateUrl: './resource-properties.component.html',
  styleUrl: './resource-properties.component.scss',
})
export class ResourcePropertiesComponent extends BasePropertiesComponent {
  private resourceService = inject(ResourceService);
  resource: Signal<Resource> = this.resourceService.resource;

  properties = computed<Property[]>(() => {
    const props = this.propertiesService.properties;
    const result: Property[] = [];
    if (this.showAppNameProperty()) {
      result.push(this.appNameProperty());
    }
    if (this.showOutOfServiceProperty()) {
      result.push(this.outOfServiceProperty());
    }
    result.push(...props());
    return result;
  });

  isLoading = this.propertiesService.isLoadingResourceProperties;

  permissions = computed(() => {
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

  protected getEntityId(): number {
    return this.resource()?.id;
  }

  protected getUnsavedChangesKey(): string {
    return 'resource-properties';
  }

  protected getEditorOptions() {
    return {
      includeResetsInHasChanges: true,
      unmarkResetOnChange: true,
    };
  }

  protected bulkUpdateProperties(
    entityId: number,
    updatedProperties: PropertyUpdate[],
    resetProperties: PropertyUpdate[],
    contextId: number,
  ) {
    return this.propertiesService.bulkUpdateResourcePropertiesValues(
      entityId,
      updatedProperties,
      resetProperties,
      contextId,
    );
  }

  protected reloadProperties(entityId: number, contextId: number): void {
    this.propertiesService.setIdsForResourceProperties(entityId, contextId);
  }

  protected getDeleteParams(): [number | undefined, number | undefined] {
    return [this.resource().id, undefined];
  }

  protected getSaveDescriptorParams(): [number | undefined, number | undefined] {
    return [this.resource().id, undefined];
  }

  private appNameProperty = computed<Property>(() => ({
    name: 'resourceName',
    displayName: `Resource name`,
    value: this.resource()?.name || '',
    replacedValue: '',
    generalComment: '',
    valueComment: 'specialProperty',
    descriptorId: -1,
    context: 'Global',
    nullable: false,
    optional: false,
  }));

  private outOfServiceProperty = computed<Property>(() => ({
    name: 'outOfService',
    displayName: 'Out Of Service',
    value: this.resource().outOfServiceReleaseName || '',
    replacedValue: '',
    generalComment: '',
    valueComment: 'staticProperty',
    descriptorId: -2,
    context: 'Global',
    optional: true,
    disabled: true,
  }));

  private showAppNameProperty = computed(() => {
    return this.contextId() === 1;
  });

  private showOutOfServiceProperty = computed(() => {
    return this.resource()?.resourceTypeId === 1;
  });
}
