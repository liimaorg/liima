import { Component, input, signal, computed, inject, effect, Signal } from '@angular/core';
import { Resource } from '../../models/resource';
import { Property } from '../../models/property';
import { ResourceService } from '../../services/resource.service';
import { PropertyFieldComponent } from '../../property-field/property-field.component';
import { ButtonComponent } from '../../../shared/button/button.component';
import { LoadingIndicatorComponent } from '../../../shared/elements/loading-indicator.component';
import { ResourcePropertiesService } from '../../services/resource-properties.service';
import { TileComponent } from '../../../shared/tile/tile.component';
import { AuthService } from '../../../auth/auth.service';
import { EnvironmentService } from '../../../deployment/environment.service';

@Component({
  selector: 'app-resource-properties',
  standalone: true,
  imports: [PropertyFieldComponent, ButtonComponent, LoadingIndicatorComponent, TileComponent],
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

  private changedProperties = signal<Map<string, string>>(new Map());

  properties = this.propertiesService.properties;

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
        canAddProperty: this.authService.hasPermission('RESOURCE', 'UPDATE', null, null, this.context().name),
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
    valueComment: '',
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
    valueComment: '',
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

  constructor() {
    effect(() => {
      this.changedProperties.set(new Map());
      this.errorMessage.set(null);
    });
  }

  // isDefinedOnInstanceOrType is only relevant for rendering editable properties
  // so there is no difference in propertytypes only for rendering the table component (releatedResourceProperties)

  hasChanges = computed(() => this.changedProperties().size > 0);

  onPropertyChange(propertyName: string, newValue: string) {
    const props = this.properties() || [];
    const originalProperty = props.find((p) => p.name === propertyName);

    if (originalProperty && originalProperty.value !== newValue) {
      this.changedProperties.update((map) => {
        const newMap = new Map(map);
        newMap.set(propertyName, newValue);
        return newMap;
      });
    } else {
      this.changedProperties.update((map) => {
        const newMap = new Map(map);
        newMap.delete(propertyName);
        return newMap;
      });
    }
  }

  resetChanges() {
    this.changedProperties.set(new Map());
    this.errorMessage.set(null);
    this.successMessage.set(null);
  }

  addProperty() {
    console.log('add property');
  }

  saveChanges() {
    const res = this.resource();
    const ctxId = this.contextId();
    if (!res?.id) return;

    const changes = this.changedProperties();
    if (changes.size === 0) return;

    this.isSaving.set(true);
    this.errorMessage.set(null);
    this.successMessage.set(null);

    const props = this.properties() || [];
    const updatedProperties: Property[] = Array.from(changes.entries()).map(([name, value]) => {
      const original = props.find((p) => p.name === name);
      return {
        ...original,
        name,
        value,
      } as Property;
    });

    this.propertiesService.bulkUpdateResourceProperties(res.id, updatedProperties, ctxId).subscribe({
      next: () => {
        this.isSaving.set(false);
        this.successMessage.set('Properties saved successfully');
        this.changedProperties.set(new Map());
        this.propertiesService.setIdsForResourceProperties(res.id, ctxId);
        setTimeout(() => this.successMessage.set(null), 3000);
      },
      error: (error) => {
        this.isSaving.set(false);
        this.errorMessage.set('Failed to save properties: ' + (error.message || 'Unknown error'));
      },
    });
  }
}
