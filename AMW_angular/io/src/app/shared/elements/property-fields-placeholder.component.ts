import { Component, computed, input } from '@angular/core';

/**
 * POC: skeleton shaped like the real two-column property fields grid (label bar + input bar per field).
 */
@Component({
  selector: 'app-property-fields-placeholder',
  standalone: true,
  template: `
    <div class="properties-list placeholder-glow" aria-hidden="true">
      <div class="properties-column">
        @for (i of leftFields(); track i) {
          <div class="property-field-placeholder">
            <span class="placeholder col-4 mb-1"></span>
            <span class="placeholder col-12 input-placeholder"></span>
          </div>
        }
      </div>
      <div class="properties-column">
        @for (i of rightFields(); track i) {
          <div class="property-field-placeholder">
            <span class="placeholder col-4 mb-1"></span>
            <span class="placeholder col-12 input-placeholder"></span>
          </div>
        }
      </div>
    </div>
  `,
  styles: [
    `
      .properties-list {
        display: grid;
        grid-template-columns: repeat(2, minmax(0, 1fr));
        column-gap: 2.5rem;
      }
      .properties-column {
        display: flex;
        flex-direction: column;
        row-gap: 0.75rem;
        min-width: 0;
      }
      .property-field-placeholder .placeholder {
        display: block;
      }
      .property-field-placeholder .input-placeholder {
        height: 1.8rem;
        margin-top: 0.25rem;
      }
    `,
  ],
})
export class PropertyFieldsPlaceholderComponent {
  fields = input<number>(4);
  protected leftFields = computed(() => Array.from({ length: Math.ceil(this.fields() / 2) }, (_, i) => i));
  protected rightFields = computed(() => Array.from({ length: Math.floor(this.fields() / 2) }, (_, i) => i));
}
