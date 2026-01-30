import { Component, input, signal, computed, inject, effect, Signal } from '@angular/core';
import { Resource } from '../../models/resource';
import { Property } from '../../models/property';
import { ResourceService } from '../../services/resource.service';
import { PropertyFieldComponent } from '../../property-field/property-field.component';
import { ButtonComponent } from '../../../shared/button/button.component';
import { LoadingIndicatorComponent } from '../../../shared/elements/loading-indicator.component';
import { ResourceTypesService } from '../../services/resource-types.service';
import { ResourceType } from '../../models/resource-type';

@Component({
  selector: 'app-resource-type-properties',
  standalone: true,
  imports: [PropertyFieldComponent, ButtonComponent, LoadingIndicatorComponent],
  templateUrl: './resource-type-properties.component.html',
  styleUrl: './resource-type-properties.component.scss',
})
export class ResourceTypePropertiesComponent {
  contextId = input.required<number>();

  private resourceTypeService = inject(ResourceTypesService);

  isSaving = signal(false);
  errorMessage = signal<string | null>(null);
  successMessage = signal<string | null>(null);
  resourceType: Signal<ResourceType> = this.resourceTypeService.resourceType;

  private changedProperties = signal<Map<string, string>>(new Map());

  properties = this.resourceTypeService.properties;

  isLoading = computed(() => {
    const ctxId = this.contextId();

    if (this.resourceType()?.id && ctxId) {
      this.resourceTypeService.setContextForProperties(this.resourceType().id, ctxId);
      return false;
    }
    return false;
  });

  // Special property for resource type name (only shown in Global context)
  resourceTypeNameProperty = computed<Property>(() => ({
    name: 'resourceTypeName',
    displayName: 'Resource Type name',
    value: this.resourceType()?.name || '',
    replacedValue: '',
    generalComment: '',
    valueComment: '',
    context: 'Global',
    nullable: false,
    optional: false,
  }));

  showResourceTypeNameProperty = computed(() => {
    return this.contextId() === 1;
  });

  constructor() {
    effect(() => {
      const ctxId = this.contextId();
      this.changedProperties.set(new Map());
      this.errorMessage.set(null);
    });
  }

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

  saveChanges() {
    const res = this.resourceType();
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

    // this.resourceTypeService.bulkUpdateProperties(res.id, updatedProperties, ctxId).subscribe({
    //   next: () => {
    //     this.isSaving.set(false);
    //     this.successMessage.set('Properties saved successfully');
    //     this.changedProperties.set(new Map());
    //     this.resourceTypeService.setContextForProperties(res.id, ctxId);
    //     setTimeout(() => this.successMessage.set(null), 3000);
    //   },
    //   error: (error) => {
    //     this.isSaving.set(false);
    //     this.errorMessage.set('Failed to save properties: ' + (error.message || 'Unknown error'));
    //   },
    // });
  }
}
