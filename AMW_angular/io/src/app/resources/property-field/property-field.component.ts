import { Component, input, output, signal, computed } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Property } from '../models/property';
import { ButtonComponent } from '../../shared/button/button.component';
import { IconComponent } from '../../shared/icon/icon.component';
import { NgbTooltip } from '@ng-bootstrap/ng-bootstrap';

@Component({
  selector: 'app-property-field',
  standalone: true,
  imports: [FormsModule, ButtonComponent, IconComponent, NgbTooltip],
  templateUrl: './property-field.component.html',
  styleUrl: './property-field.component.scss',
})
export class PropertyFieldComponent {
  property = input.required<Property>();
  mode = input<'resource' | 'resourceType'>('resource');
  canEdit = input<boolean>(false);
  canDelete = input<boolean>(false);
  hideTooltip = input<boolean>(false);
  valueChange = output<string>();
  resetChange = output<boolean>();
  editClicked = output<number>();
  deleteClicked = output<number>();

  private internalValue = signal<string | null>(null);
  validationError = signal<string | null>(null);
  touched = signal(false);
  resetChecked = signal(false);

  fieldId = computed(() => `property-${this.property().name}`);
  resetId = computed(() => `reset-${this.property().name}`);
  displayLabel = computed(() => this.property().displayName || this.property().name);
  hasError = computed(() => this.touched() && !!this.validationError());
  canReset = computed(() => !!this.property().definedInContext);
  isInputDisabled = computed(() => this.property().disabled || this.resetChecked());

  showProperty = computed(() => {
    return this.property().cardinality === null || this.property().cardinality != -1;
  });

  isEditable = computed(() => {
    const prop = this.property();
    const isResourceContext = this.mode() === 'resource';
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
    const current = this.internalValue();
    if (current !== null) return current;
    return this.property().value ?? '';
  }

  set localValue(value: string) {
    this.internalValue.set(value);
  }

  onBlur() {
    this.touched.set(true);
    this.validate();
    this.valueChange.emit(this.localValue);
  }

  protected toggleReset(event: Event) {
    const target = event.target as HTMLInputElement | null;
    const checked = !!target?.checked;
    this.resetChecked.set(checked);

    if (checked) {
      // Reset means: value is taken from parent context (replacedValue)
      this.internalValue.set(this.property().replacedValue || '');
    } else {
      // Back to the value defined in the current context
      this.internalValue.set(this.property().value || '');
    }

    this.touched.set(true);
    this.validate();
    this.resetChange.emit(checked);
    this.valueChange.emit(this.localValue);
  }

  /*
     Validation behavior:
     - "No value" is an error only if `required` AND `value+default+mik` are all empty.
     - Regex matching uses `defaultValue` if `value` is empty.
     - If (`nullable` OR `optional`) AND `value+default` are empty AND `mik` is present => skip regex validation.
     -> mik (machineInterpretationKey) -> liima might produce a value later 🤯
   */
  private validate() {
    const prop = this.property();

    const isNullable = !!prop.nullable;
    const isOptional = !!prop.optional;
    const defaultValue = prop.defaultValue ?? '';
    const mik = prop.mik ?? '';
    const regexString = prop.validationRegex ?? '.*';

    const noValueSet = this.localValue === '' && defaultValue === '';

    if (!isNullable && !isOptional && noValueSet && mik === '') {
      this.validationError.set('This field is required');
      return;
    }

    if ((isNullable || isOptional) && noValueSet && mik !== '') {
      this.validationError.set(null);
      return;
    }

    const regexMatchValue = this.localValue === '' ? defaultValue : this.localValue;

    if (regexString && regexMatchValue !== '') {
      try {
        // enforce "full match".
        const fullMatchRegex = new RegExp(`^(?:${regexString})$`);
        if (!fullMatchRegex.test(regexMatchValue)) {
          this.validationError.set('Value does not match the required pattern');
          return;
        }
      } catch (e) {
        console.error('Invalid regex pattern:', regexString, e);
        this.validationError.set('Value does not match the required pattern');
        return;
      }
    }

    this.validationError.set(null);
  }

  protected propertyEdit(descriptorId: number) {
    this.editClicked.emit(descriptorId);
  }

  protected propertyDelete(descriptorId: number) {
    this.deleteClicked.emit(descriptorId);
  }

  protected readonly HTMLInputElement = HTMLInputElement;
}
