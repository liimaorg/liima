import { Component, input, signal, computed, inject, Signal } from '@angular/core';
import { Property } from '../../models/property';
import { PropertyFieldComponent } from '../../property-field/property-field.component';
import { ButtonComponent } from '../../../shared/button/button.component';
import { LoadingIndicatorComponent } from '../../../shared/elements/loading-indicator.component';
import { ResourceTypesService } from '../../services/resource-types.service';
import { ResourceType } from '../../models/resource-type';
import { ResourcePropertiesService } from '../../services/resource-properties.service';
import { TileComponent } from '../../../shared/tile/tile.component';
import { AuthService } from '../../../auth/auth.service';
import { createPropertiesEditor } from '../../properties-editor';
import { EnvironmentService } from '../../../deployment/environment.service';
import { PropertiesListComponent } from '../../properties-list/properties-list.component';
import { PropertiesPanelComponent } from '../../properties-panel/properties-panel.component';

@Component({
  selector: 'app-resource-type-properties',
  standalone: true,
  imports: [LoadingIndicatorComponent, TileComponent, PropertiesListComponent, PropertiesPanelComponent],
  templateUrl: './resource-type-properties.component.html',
  styleUrl: './resource-type-properties.component.scss',
})
export class ResourceTypePropertiesComponent {
  contextId = input.required<number>();

  private authService = inject(AuthService);
  private resourceTypeService = inject(ResourceTypesService);
  private propertiesService = inject(ResourcePropertiesService);
  private environmentsService = inject(EnvironmentService);

  isSaving = signal(false);
  errorMessage = signal<string | null>(null);
  successMessage = signal<string | null>(null);
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

  // TODO check behavier in JSF
  private editor = createPropertiesEditor(() => [...this.properties().filter((p) => !p.disabled)], {
    // preserve current behavior: hasChanges only considers changed values (not reset toggles)
    includeResetsInHasChanges: false,
    // preserve current behavior: editing a property does not auto-uncheck a previous reset
    unmarkResetOnChange: false,
  });

  hasChanges = this.editor.hasChanges;
  resetToken = this.editor.resetToken;

  isLoading = computed(() => {
    if (this.resourceType()?.id && this.contextId()) {
      this.propertiesService.setIdsForResourceTypeProperties(this.resourceType().id, this.contextId());
      return false;
    }
    return false;
  });

  context = computed(() => {
    return this.environmentsService.findEnvironmentById(this.environmentsService.environmentTree(), this.contextId());
  });

  // same permissions for crud c-- TODO verify for resource-type + add context
  permissions = computed(() => {
    if (this.authService.restrictions().length > 0) {
      return {
        canAddProperty: this.authService.hasPermission('RESOURCE', 'UPDATE', null, null, this.context()?.name),
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
    valueComment: 'staticProperty',
    context: 'Global',
    nullable: false,
    optional: false,
  }));

  showResourceTypeNameProperty = computed(() => {
    return this.contextId() === 1;
  });

  protected addProperty() {}

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

  saveChanges() {
    const changes = this.editor.changedProperties();
    const resets = this.editor.resetProperties();
    if (changes.size === 0 && resets.size === 0) return;

    this.isSaving.set(true);
    this.errorMessage.set(null);
    this.successMessage.set(null);

    const updatedProperties: Property[] = Array.from(changes.entries()).map(([name, value]) => {
      const original = this.properties().find((p) => p.name === name);
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
    //     this.editor.resetChanges();
    //     this.propertiesService.setIdsForResourceTypeProperties(res.id, ctxId);
    //     setTimeout(() => this.successMessage.set(null), 3000);
    //   },
    //   error: (error) => {
    //     this.isSaving.set(false);
    //     this.errorMessage.set('Failed to save properties: ' + (error.message || 'Unknown error'));
    //   },
    // });
  }
}
