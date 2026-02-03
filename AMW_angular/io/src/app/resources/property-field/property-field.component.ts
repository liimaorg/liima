import { Component, input, output, signal, computed } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Property } from '../models/property';
import { ButtonComponent } from '../../shared/button/button.component';
import { IconComponent } from '../../shared/icon/icon.component';

@Component({
  selector: 'app-property-field',
  standalone: true,
  imports: [FormsModule, ButtonComponent, IconComponent],
  templateUrl: './property-field.component.html',
  styleUrl: './property-field.component.scss',
})
export class PropertyFieldComponent {
  property = input.required<Property>();
  isResource = input<boolean>(true); // true for resource component, false for resourceType component
  canEdit = input<boolean>(false);
  canDelete = input<boolean>(false);
  valueChange = output<string>();

  private internalValue = signal<string>('');
  validationError = signal<string | null>(null);
  touched = signal(false);

  fieldId = computed(() => `property-${this.property().name}`);
  displayLabel = computed(() => this.property().displayName || this.property().name);
  hasError = computed(() => this.touched() && !!this.validationError());

  showProperty = computed(() => {
    return this.property().cardinality === null || this.property().cardinality != -1;
  });

  isEditable = computed(() => {
    const prop = this.property();
    const isResourceContext = this.isResource();
    const origin = prop.propertyDescriptorOrigin;

    if (!origin) {
      return false;
    }

    // Editable when:
    // - Resource context + descriptor origin is INSTANCE
    // - ResourceType context + descriptor origin is TYPE
    return isResourceContext ? origin === 'INSTANCE' : origin === 'TYPE';
  });

  propertyEditLink = computed(() => {
    const prop = this.property();
    // TODO: Build proper edit link with descriptorId and other params
    return `/properties/edit/${prop.descriptorId}`;
  });

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

  protected propertyEdit(descriptorId: number) {
    // TODO
    console.log('propertyEdit' + descriptorId);
  }

  protected propertyDelete(descriptorId: number) {
    // TODO
    console.log('propertyDelete' + descriptorId);
  }
}
