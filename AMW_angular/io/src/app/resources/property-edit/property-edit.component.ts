import { ChangeDetectionStrategy, Component, computed, effect, inject, output, Signal, signal } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { ModalHeaderComponent } from '../../shared/modal-header/modal-header.component';
import { NgOptionComponent, NgSelectComponent } from '@ng-select/ng-select';
import { FormControl, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { PropertyType } from '../../settings/property-types/property-type';
import { PropertyTag } from '../../settings/property-types/property-tag';
import { PropertyDescriptor } from '../models/property-descriptor';
import { PropertyTypesService } from '../../settings/property-types/property-types.service';
import { PropertyDescriptorService } from '../services/property-descriptor.service';
import { TagInputComponent } from '../../shared/tag-input/tag-input.component';
import { map, startWith } from 'rxjs/operators';
import { toSignal } from '@angular/core/rxjs-interop';
import { ButtonComponent } from '../../shared/button/button.component';
import { LoadingIndicatorComponent } from '../../shared/elements/loading-indicator.component';

interface PropertyDescriptorForm {
  name: FormControl<string>;
  displayName: FormControl<string>;
  validationRegex: FormControl<string>;
  nullable: FormControl<boolean>;
  optional: FormControl<boolean>;
  encrypted: FormControl<boolean>;
  mik: FormControl<string>;
  defaultValue: FormControl<string>;
  exampleValue: FormControl<string>;
  comment: FormControl<string>;
}

@Component({
  selector: 'app-property-edit',
  imports: [
    ButtonComponent,
    LoadingIndicatorComponent,
    ModalHeaderComponent,
    NgOptionComponent,
    NgSelectComponent,
    ReactiveFormsModule,
    TagInputComponent,
    FormsModule,
  ],
  templateUrl: './property-edit.component.html',
  styleUrls: ['./property-edit.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PropertyEditComponent {
  activeModal = inject(NgbActiveModal);
  private propertyTypesService = inject(PropertyTypesService);
  private descriptorService = inject(PropertyDescriptorService);

  private _descriptorId = signal<number | null>(null);
  private _canEdit = signal<boolean>(true);
  private _canDecrypt = signal<boolean>(false);

  canEdit = this._canEdit.asReadonly();

  propertyDescriptor = this.descriptorService.propertyDescriptor;
  isLoadingDescriptor = this.descriptorService.isLoadingDescriptor;
  errorMessage = signal<string | null>(null);

  saveDescriptor = output<PropertyDescriptor>();

  form: FormGroup<PropertyDescriptorForm>;

  // Add 'Custom' property type to the list
  propertyTypes = computed(() => {
    const customType: PropertyType = {
      id: 0,
      name: 'Custom',
      encrypted: false,
      validationRegex: '.*',
      propertyTags: [],
    };
    return [customType, ...this.propertyTypesService.propertyTypes()];
  });

  selectedPropertyType = signal<PropertyType | null>(null);
  tags = signal<PropertyTag[]>([]);

  // Form validity as signal - will be initialized in constructor
  formValid!: Signal<boolean>;

  isNewMode = computed(() => !this.propertyDescriptor()?.id);
  title = computed(() => (this.isNewMode() ? 'New Property Descriptor' : 'Edit Property Descriptor'));

  isLongDefaultValue(): boolean {
    const val = this.form.controls.defaultValue.value;
    return !!val && val.length > 70;
  }

  isLongExampleValue(): boolean {
    const val = this.form.controls.exampleValue.value;
    return !!val && val.length > 70;
  }

  isLongMik(): boolean {
    const val = this.form.controls.mik.value;
    return !!val && val.length > 70;
  }

  constructor() {
    effect(() => {
      const id = this._descriptorId();
      if (id && id > 0) {
        this.descriptorService.loadPropertyDescriptor(id);
      } else {
        // For new descriptors, set 'Custom' (ID 0) as default property type
        const customType = this.propertyTypes().find((t) => t.id === 0);
        if (customType) {
          this.selectedPropertyType.set(customType);
        }
      }
    });

    this.form = new FormGroup<PropertyDescriptorForm>({
      name: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
      displayName: new FormControl('', { nonNullable: true }),
      validationRegex: new FormControl('.*', { nonNullable: true, validators: [Validators.required] }),
      nullable: new FormControl(false, { nonNullable: true }),
      optional: new FormControl(false, { nonNullable: true }),
      encrypted: new FormControl(false, { nonNullable: true }),
      mik: new FormControl('', { nonNullable: true }),
      defaultValue: new FormControl('', { nonNullable: true }),
      exampleValue: new FormControl('', { nonNullable: true }),
      comment: new FormControl('', { nonNullable: true }),
    });

    this.formValid = toSignal(
      this.form.statusChanges.pipe(
        startWith(this.form.status),
        map(() => this.form.valid),
      ),
      { initialValue: false },
    );

    effect(() => {
      const property = this.propertyDescriptor();
      const descriptorId = this._descriptorId();

      // Only patch form if we're editing an existing descriptor AND it matches the current ID
      if (property && descriptorId && property.id === descriptorId) {
        this.form.patchValue({
          name: property.name,
          displayName: property.displayName || '',
          validationRegex: property.validationRegex || '.*',
          nullable: property.nullable ?? false,
          optional: property.optional ?? false,
          encrypted: property.encrypted ?? false,
          mik: property.mik || '',
          defaultValue: property.defaultValue || '',
          exampleValue: property.exampleValue || '',
          comment: property.comment || '',
        });

        const matchedType = this.findMatchingPropertyType(property);
        if (matchedType) {
          this.selectedPropertyType.set(matchedType);
        }

        this.tags.set([...property.propertyTags]);
      }
    });

    effect(() => {
      if (!this._canEdit()) {
        this.form.disable();
      } else {
        this.form.enable();
        if (!this._canDecrypt()) {
          this.form.controls.encrypted.disable();
        }
      }
    });
  }

  configure(config: { descriptorId: number | null; canEdit: boolean; canDecrypt: boolean }): void {
    // Reset ALL state before configuring
    this.form.reset({
      name: '',
      displayName: '',
      validationRegex: '.*',
      nullable: false,
      optional: false,
      encrypted: false,
      mik: '',
      defaultValue: '',
      exampleValue: '',
      comment: '',
    });
    this.tags.set([]);
    this.errorMessage.set(null);
    this.selectedPropertyType.set(null);

    // Set configuration - this will trigger effects to load data or set defaults
    this._descriptorId.set(config.descriptorId);
    this._canEdit.set(config.canEdit);
    this._canDecrypt.set(config.canDecrypt);
  }

  private findMatchingPropertyType(property: PropertyDescriptor): PropertyType | null {
    // If propertyTypeEntity is provided, use it directly
    if (property.propertyTypeEntity) {
      return property.propertyTypeEntity;
    }

    // Try exact match: validation regex + encrypted flag
    const exactMatch = this.propertyTypes().find(
      (t) => t.validationRegex === property.validationRegex && t.encrypted === property.encrypted,
    );
    if (exactMatch) {
      return exactMatch;
    }

    // Fallback: match by validation regex only
    return this.propertyTypes().find((t) => t.validationRegex === property.validationRegex) ?? null;
  }

  onPropertyTypeChange(typeId: number) {
    const selectedType = this.propertyTypes().find((t) => t.id === typeId);
    if (selectedType) {
      this.selectedPropertyType.set(selectedType);
      this.form.controls.validationRegex.setValue(selectedType.validationRegex);
      this.form.controls.encrypted.setValue(selectedType.encrypted);

      // Merge property type tags with existing tags
      const existingTags = this.tags();
      const existingTagNames = new Set(existingTags.map((t) => t.name));
      const newTags = selectedType.propertyTags.filter((t) => !existingTagNames.has(t.name));
      if (newTags.length > 0) {
        this.tags.set([...existingTags, ...newTags]);
      }
    }
  }

  onTagsChange(updatedTags: PropertyTag[]) {
    this.tags.set(updatedTags);
  }

  cancel() {
    this.activeModal.dismiss();
  }

  save() {
    if (this.formValid() && this.selectedPropertyType()) {
      const descriptor: PropertyDescriptor = {
        ...this.form.getRawValue(),
        propertyTypeEntity: this.selectedPropertyType()!,
        propertyTags: this.tags(),
        id: this._descriptorId() || undefined,
      };
      this.saveDescriptor.emit(descriptor);
    }
  }
}
