import { Component, input, signal, computed, inject, Signal } from '@angular/core';
import { forkJoin, of } from 'rxjs';
import { Resource } from '../../models/resource';
import { Property } from '../../models/property';
import { ResourceService } from '../../services/resource.service';
import { LoadingIndicatorComponent } from '../../../shared/elements/loading-indicator.component';
import { PropertyUpdate, ResourcePropertiesService } from '../../services/resource-properties.service';
import { TileComponent } from '../../../shared/tile/tile.component';
import { AuthService } from '../../../auth/auth.service';
import { EnvironmentService } from '../../../deployment/environment.service';
import { PropertiesPanelComponent } from '../../properties-panel/properties-panel.component';
import { PropertiesListComponent } from '../../properties-list/properties-list.component';
import { createPropertiesEditor } from '../../properties-editor';

@Component({
  selector: 'app-resource-properties',
  standalone: true,
  imports: [LoadingIndicatorComponent, TileComponent, PropertiesPanelComponent, PropertiesListComponent],
  templateUrl: './resource-properties.component.html',
  styleUrl: './resource-properties.component.scss',
})
export class ResourcePropertiesComponent {
  contextId = input.required<number>();

  private authService = inject(AuthService);
  private resourceService = inject(ResourceService);
  private propertiesService = inject(ResourcePropertiesService);
  private environmentsService = inject(EnvironmentService);

  isSaving = signal(false);
  errorMessage = signal<string | null>(null);
  successMessage = signal<string | null>(null);
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

  private editor = createPropertiesEditor(() => [...this.properties().filter((p) => !p.disabled)], {
    includeResetsInHasChanges: true,
    unmarkResetOnChange: true,
  });

  hasChanges = this.editor.hasChanges;
  resetToken = this.editor.resetToken;

  isLoading = computed(() => {
    if (this.resource()?.id && this.contextId()) {
      this.propertiesService.setIdsForResourceProperties(this.resource().id, this.contextId());
      return false;
    }
    return false;
  });

  context = computed(() => {
    return this.environmentsService.findEnvironmentById(this.environmentsService.environmentTree(), this.contextId());
  });

  // same permissions for crud
  permissions = computed(() => {
    if (this.authService.restrictions().length > 0) {
      return {
        canAddProperty: this.authService.hasPermission('RESOURCE', 'UPDATE', null, null, this.context()?.name),
      };
    } else {
      return { canAddProperty: false };
    }
  });

  // Special property for resource/resource type name (only shown in Global context)
  appNameProperty = computed<Property>(() => ({
    name: 'resourceName',
    displayName: `${this.resource()?.type || 'Resource'} name`,
    value: this.resource()?.name || '',
    replacedValue: '',
    generalComment: '',
    valueComment: 'specialProperty',
    descriptorId: -1,
    context: 'Global',
    nullable: false,
    optional: false,
  }));

  // Special property for Out Of Service (only shown for applications, always disabled)
  outOfServiceProperty = computed<Property>(() => ({
    name: 'outOfService',
    displayName: 'Out Of Service',
    value: '', // TODO: Get from resource.resourceGroup.outOfServiceRelease.name when available
    replacedValue: '',
    generalComment: '',
    valueComment: 'staticProperty',
    descriptorId: -2,
    context: 'Global',
    optional: true,
    disabled: true,
  }));

  showAppNameProperty = computed(() => {
    return this.contextId() === 1;
  });

  showOutOfServiceProperty = computed(() => {
    return this.resource()?.resourceTypeId === 1;
  });

  onPropertyChange(propertyName: string, newValue: string) {
    this.editor.onPropertyChange(propertyName, newValue);
  }

  onPropertyReset(propertyName: string, checked: boolean) {
    this.editor.onPropertyReset(propertyName, checked);
  }

  resetChanges() {
    this.editor.resetChanges();
    this.errorMessage.set(null);
    this.successMessage.set(null);
  }

  addProperty() {
    console.log('add property');
  }

  saveChanges() {
    const changes = this.editor.changedProperties();
    const resets = this.editor.resetProperties();
    if (changes.size === 0 && resets.size === 0) return;

    this.isSaving.set(true);
    this.errorMessage.set(null);
    this.successMessage.set(null);

    const updatedProperties: PropertyUpdate[] = Array.from(changes.entries()).map(([name, value]) => ({
      name,
      value,
    }));

    const resetProperties: PropertyUpdate[] = Array.from(resets.entries()).map(([name, value]) => ({
      name,
      value,
    }));

    const update$ =
      updatedProperties.length + resetProperties.length
        ? this.propertiesService.bulkUpdateResourcePropertiesValues(
            this.resource().id,
            updatedProperties,
            resetProperties,
            this.contextId(),
          )
        : of(void 0);

    forkJoin([update$]).subscribe({
      next: () => {
        this.isSaving.set(false);
        this.successMessage.set('Properties saved successfully');
        this.editor.resetChanges();
        this.propertiesService.setIdsForResourceProperties(this.resource().id, this.contextId());
        setTimeout(() => this.successMessage.set(null), 3000);
      },
      error: (error) => {
        this.isSaving.set(false);
        this.errorMessage.set('Failed to save properties: ' + (error.message || 'Unknown error'));
      },
    });
  }
}
