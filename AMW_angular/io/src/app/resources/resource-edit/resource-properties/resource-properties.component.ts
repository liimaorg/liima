import { Component, input, signal, computed, inject, OnInit, OnChanges, SimpleChanges } from '@angular/core';
import { Resource } from '../../models/resource';
import { Property } from '../../models/property';
import { ResourceService } from '../../services/resource.service';
import { PropertyFieldComponent } from './property-field.component';
import { ButtonComponent } from '../../../shared/button/button.component';
import { LoadingIndicatorComponent } from '../../../shared/elements/loading-indicator.component';

@Component({
  selector: 'app-resource-properties',
  standalone: true,
  imports: [PropertyFieldComponent, ButtonComponent, LoadingIndicatorComponent],
  template: `
    <div class="resource-properties-container">
      <div class="properties-header">
        <h3>Properties</h3>
        @if (hasChanges()) {
          <div class="action-buttons">
            <app-button [variant]="'secondary'" [size]="'sm'" (click)="resetChanges()">Cancel</app-button>
            <app-button [variant]="'primary'" [size]="'sm'" (click)="saveChanges()" [disabled]="isSaving()">
              {{ isSaving() ? 'Saving...' : 'Save Changes' }}
            </app-button>
          </div>
        }
      </div>

      <app-loading-indicator [isLoading]="isLoading()"></app-loading-indicator>

      @if (errorMessage()) {
        <div class="error-banner">{{ errorMessage() }}</div>
      }

      @if (successMessage()) {
        <div class="success-banner">{{ successMessage() }}</div>
      }

      @if (!isLoading() && standardProperties().length > 0) {
        <div class="properties-section">
          <h4 class="section-title">Standard Properties</h4>
          <div class="properties-grid">
            @for (property of standardProperties(); track property.name) {
              <app-property-field
                [property]="property"
                (valueChange)="onPropertyChange(property.name, $event)"
              ></app-property-field>
            }
          </div>
        </div>
      }

      @if (!isLoading() && customProperties().length > 0) {
        <div class="properties-section">
          <h4 class="section-title">Custom Properties</h4>
          <div class="properties-grid">
            @for (property of customProperties(); track property.name) {
              <app-property-field
                [property]="property"
                (valueChange)="onPropertyChange(property.name, $event)"
              ></app-property-field>
            }
          </div>
        </div>
      }

      @if (!isLoading() && standardProperties().length === 0 && customProperties().length === 0) {
        <div class="no-properties">No properties defined for this resource</div>
      }
    </div>
  `,
  styles: [
    `
      .resource-properties-container {
        background: white;
        border: 1px solid #ddd;
        border-radius: 4px;
        padding: 1.5rem;
        margin-bottom: 1.5rem;
      }

      .properties-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 1.5rem;
        padding-bottom: 1rem;
        border-bottom: 1px solid #ddd;
      }

      .properties-header h3 {
        margin: 0;
        font-size: 20px;
        font-weight: 600;
      }

      .action-buttons {
        display: flex;
        gap: 0.5rem;
      }

      .properties-section {
        margin-bottom: 2rem;
      }

      .properties-section:last-child {
        margin-bottom: 0;
      }

      .section-title {
        font-size: 16px;
        font-weight: 600;
        margin-bottom: 1rem;
        color: #495057;
        padding-bottom: 0.5rem;
        border-bottom: 1px solid #e9ecef;
      }

      .properties-grid {
        display: grid;
        grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
        gap: 1rem;
      }

      .no-properties {
        text-align: center;
        padding: 2rem;
        color: #6c757d;
        font-style: italic;
      }

      .error-banner {
        background-color: #f8d7da;
        color: #721c24;
        padding: 0.75rem 1rem;
        border-radius: 4px;
        margin-bottom: 1rem;
        border: 1px solid #f5c6cb;
      }

      .success-banner {
        background-color: #d4edda;
        color: #155724;
        padding: 0.75rem 1rem;
        border-radius: 4px;
        margin-bottom: 1rem;
        border: 1px solid #c3e6cb;
      }
    `,
  ],
})
export class ResourcePropertiesComponent implements OnInit, OnChanges {
  resource = input.required<Resource>();
  contextId = input.required<number>();

  private resourceService = inject(ResourceService);

  isLoading = signal(true);
  isSaving = signal(false);
  errorMessage = signal<string | null>(null);
  successMessage = signal<string | null>(null);

  private changedProperties = signal<Map<string, string>>(new Map());

  private propertiesData = signal<Property[]>([]);

  ngOnInit() {
    this.loadProperties();
  }

  ngOnChanges(changes: SimpleChanges) {
    if (changes['contextId'] && !changes['contextId'].firstChange) {
      this.changedProperties.set(new Map());
      this.loadProperties();
    }
  }

  private loadProperties() {
    const res = this.resource();
    const ctxId = this.contextId();

    if (!res?.id) {
      this.isLoading.set(false);
      return;
    }

    this.isLoading.set(true);
    this.errorMessage.set(null);

    this.resourceService.getProperties(res.id, ctxId).subscribe({
      next: (properties) => {
        this.propertiesData.set(properties);
        this.isLoading.set(false);
      },
      error: (error) => {
        this.isLoading.set(false);
        this.errorMessage.set('Failed to load properties: ' + (error.message || 'Unknown error'));
      },
    });
  }

  standardProperties = computed(() => {
    const props = this.propertiesData();
    return props
      .filter((p) => !p.definedOnInstance)
      .sort((a, b) => {
        const nameA = a.displayName || a.name;
        const nameB = b.displayName || b.name;
        return nameA.localeCompare(nameB);
      });
  });

  customProperties = computed(() => {
    const props = this.propertiesData();
    return props
      .filter((p) => p.definedOnInstance)
      .sort((a, b) => {
        const nameA = a.displayName || a.name;
        const nameB = b.displayName || b.name;
        return nameA.localeCompare(nameB);
      });
  });

  hasChanges = computed(() => this.changedProperties().size > 0);

  onPropertyChange(propertyName: string, newValue: string) {
    const props = this.propertiesData();
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
    const res = this.resource();
    const ctxId = this.contextId();
    if (!res?.id) return;

    const changes = this.changedProperties();
    if (changes.size === 0) return;

    this.isSaving.set(true);
    this.errorMessage.set(null);
    this.successMessage.set(null);

    const props = this.propertiesData();
    const updatedProperties: Property[] = Array.from(changes.entries()).map(([name, value]) => {
      const original = props.find((p) => p.name === name);
      return {
        ...original,
        name,
        value,
      } as Property;
    });

    this.resourceService.bulkUpdateProperties(res.id, updatedProperties, ctxId).subscribe({
      next: () => {
        this.isSaving.set(false);
        this.successMessage.set('Properties saved successfully');
        this.changedProperties.set(new Map());
        this.loadProperties();
        setTimeout(() => this.successMessage.set(null), 3000);
      },
      error: (error) => {
        this.isSaving.set(false);
        this.errorMessage.set('Failed to save properties: ' + (error.message || 'Unknown error'));
      },
    });
  }
}
