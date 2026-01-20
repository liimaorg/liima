import { Component, input, output, signal, computed } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Property } from '../../models/property';

@Component({
  selector: 'app-property-field',
  standalone: true,
  imports: [FormsModule],
  template: `
    <div class="property-field">
      <label [for]="fieldId()" class="property-label">
        {{ displayLabel() }}
        @if (!property().nullable && !property().optional) {
          <span class="required">*</span>
        }
      </label>
      
      @if (property().encrypted) {
        <input
          [id]="fieldId()"
          type="password"
          class="property-input"
          [class.error]="hasError()"
          [(ngModel)]="localValue"
          (blur)="onBlur()"
          [placeholder]="property().exampleValue || ''"
        />
      } @else {
        <input
          [id]="fieldId()"
          type="text"
          class="property-input"
          [class.error]="hasError()"
          [(ngModel)]="localValue"
          (blur)="onBlur()"
          [placeholder]="property().exampleValue || ''"
        />
      }
      
      @if (validationError()) {
        <div class="validation-error">{{ validationError() }}</div>
      }
      
      @if (property().defaultValue && !localValue) {
        <div class="default-value-hint">Default: {{ property().defaultValue }}</div>
      }
    </div>
  `,
  styles: [
    `
      .property-field {
        margin-bottom: 1rem;
      }

      .property-label {
        display: block;
        font-weight: 500;
        margin-bottom: 0.25rem;
        font-size: 14px;
        color: #333;
      }

      .required {
        color: #dc3545;
        margin-left: 2px;
      }

      .property-input {
        width: 100%;
        padding: 0.5rem;
        border: 1px solid #ddd;
        border-radius: 4px;
        font-size: 14px;
        transition: border-color 0.2s;
      }

      .property-input:focus {
        outline: none;
        border-color: #007bff;
        box-shadow: 0 0 0 3px rgba(0, 123, 255, 0.1);
      }

      .property-input.error {
        border-color: #dc3545;
      }

      .property-input.error:focus {
        box-shadow: 0 0 0 3px rgba(220, 53, 69, 0.1);
      }

      .validation-error {
        color: #dc3545;
        font-size: 12px;
        margin-top: 0.25rem;
      }

      .default-value-hint {
        color: #6c757d;
        font-size: 12px;
        margin-top: 0.25rem;
        font-style: italic;
      }
    `,
  ],
})
export class PropertyFieldComponent {
  property = input.required<Property>();
  valueChange = output<string>();

  private internalValue = signal<string>('');
  validationError = signal<string | null>(null);
  touched = signal(false);

  fieldId = computed(() => `property-${this.property().name}`);
  displayLabel = computed(() => this.property().displayName || this.property().name);
  hasError = computed(() => this.touched() && !!this.validationError());

  get localValue(): string {
    if (this.internalValue() === '' && !this.touched()) {
      return this.property().value || '';
    }
    return this.internalValue();
  }

  set localValue(value: string) {
    this.internalValue.set(value);
  }

  onBlur() {
    this.touched.set(true);
    this.validate();
    this.valueChange.emit(this.localValue);
  }

  private validate() {
    const value = this.localValue;
    const prop = this.property();
    const effectiveValue = value || prop.defaultValue || '';

    if (!prop.nullable && !prop.optional && !effectiveValue) {
      this.validationError.set('This field is required');
      return;
    }

    if (effectiveValue && prop.validationRegex) {
      try {
        const regex = new RegExp(prop.validationRegex);
        if (!regex.test(effectiveValue)) {
          this.validationError.set('Value does not match the required pattern');
          return;
        }
      } catch (e) {
        console.error('Invalid regex pattern:', prop.validationRegex, e);
      }
    }

    this.validationError.set(null);
  }
}
