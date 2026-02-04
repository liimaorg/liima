import { Component, input, signal, computed, inject, effect, Signal } from '@angular/core';
import { Property } from '../../models/property';
import { PropertyFieldComponent } from '../../property-field/property-field.component';
import { ButtonComponent } from '../../../shared/button/button.component';
import { LoadingIndicatorComponent } from '../../../shared/elements/loading-indicator.component';
import { ResourceTypesService } from '../../services/resource-types.service';
import { ResourceType } from '../../models/resource-type';
import { ResourcePropertiesService } from '../../services/resource-properties.service';
import { TileComponent } from '../../../shared/tile/tile.component';
import { AuthService } from '../../../auth/auth.service';

@Component({
  selector: 'app-resource-type-properties',
  standalone: true,
  imports: [PropertyFieldComponent, ButtonComponent, LoadingIndicatorComponent, TileComponent],
  templateUrl: './resource-type-properties.component.html',
  styleUrl: './resource-type-properties.component.scss',
})
export class ResourceTypePropertiesComponent {
  contextId = input.required<number>();

  private authService = inject(AuthService);
  private resourceTypeService = inject(ResourceTypesService);
  private propertiesService = inject(ResourcePropertiesService);

  isSaving = signal(false);
  errorMessage = signal<string | null>(null);
  successMessage = signal<string | null>(null);
  resourceType: Signal<ResourceType> = this.resourceTypeService.resourceType;

  private changedProperties = signal<Map<string, string>>(new Map());
  private resetProperties = signal<Set<string>>(new Set());

  properties = this.propertiesService.properties;

  isLoading = computed(() => {
    const ctxId = this.contextId();

    if (this.resourceType()?.id && ctxId) {
      this.propertiesService.setIdsForResourceTypeProperties(this.resourceType().id, ctxId);
      return false;
    }
    return false;
  });

  // same permissions for crud c-- TODO verify for resource-type + add context
  permissions = computed(() => {
    if (this.authService.restrictions().length > 0) {
      return {
        canAddProperty: this.authService.hasPermission('RESOURCE', 'UPDATE', null, null, null),
      };
    } else {
      return { canAddProperty: false };
    }
  });

  // Special property for resource type name (only shown in Global context)
  resourceTypeNameProperty = computed<Property>(() => ({
    name: 'resourceTypeName',
    displayName: 'Resource Type name',
    value: this.resourceType()?.name || '',
    replacedValue: '',
    generalComment: '',
    valueComment: '',
    defaultValue: '',
    context: 'Global',
    nullable: false,
    optional: false,
  }));

  showResourceTypeNameProperty = computed(() => {
    return this.contextId() === 1;
  });

  constructor() {
    effect(() => {
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

    // this.propertiesService.bulkUpdateResourceTypeProperties(res.id, updatedProperties, ctxId).subscribe({
    //   next: () => {
    //     this.isSaving.set(false);
    //     this.successMessage.set('Properties saved successfully');
    //     this.changedProperties.set(new Map());
    //     this.propertiesService.setIdsForResourceTypeProperties(res.id, ctxId);
    //     setTimeout(() => this.successMessage.set(null), 3000);
    //   },
    //   error: (error) => {
    //     this.isSaving.set(false);
    //     this.errorMessage.set('Failed to save properties: ' + (error.message || 'Unknown error'));
    //   },
    // });
  }

  protected addProperty() {}

  onPropertyReset(propertyName: string, checked: boolean) {
    if (checked) {
      this.resetProperties.update((set) => {
        const next = new Set(set);
        next.add(propertyName);
        return next;
      });

      this.changedProperties.update((map) => {
        if (!map.has(propertyName)) return map;
        const next = new Map(map);
        next.delete(propertyName);
        return next;
      });
    } else {
      this.resetProperties.update((set) => {
        if (!set.has(propertyName)) return set;
        const next = new Set(set);
        next.delete(propertyName);
        return next;
      });
    }
  }

  resetChanges() {
    this.changedProperties.set(new Map());
    this.resetProperties.set(new Set());
    this.errorMessage.set(null);
    this.successMessage.set(null);
  }
}
