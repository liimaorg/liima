import { ChangeDetectionStrategy, Component, computed, effect, inject, output, signal } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { ButtonComponent } from '../../shared/button/button.component';
import { ModalHeaderComponent } from '../../shared/modal-header/modal-header.component';
import { NgOptionComponent, NgSelectComponent } from '@ng-select/ng-select';
import { FormControl, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { PropertyType } from '../../settings/property-types/property-type';
import { PropertyTag } from '../../settings/property-types/property-tag';
import { CommonModule } from '@angular/common';
import { PropertyDescriptor } from '../models/property-descriptor';
import { PropertyTypesService } from '../../settings/property-types/property-types.service';
import { PropertyDescriptorService } from '../services/property-descriptor.service';
import { TagInputComponent } from '../../shared/tag-input/tag-input.component';

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
    ModalHeaderComponent,
    NgOptionComponent,
    NgSelectComponent,
    ReactiveFormsModule,
    CommonModule,
    TagInputComponent,
    FormsModule,
  ],
  templateUrl: './property-edit.component.html',
  styleUrl: './property-edit.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PropertyEditComponent {
  activeModal = inject(NgbActiveModal);
  private propertyTypesService = inject(PropertyTypesService);
  private descriptorService = inject(PropertyDescriptorService);

  private _descriptorId = signal<number | null>(null);
  descriptorId = this._descriptorId.asReadonly();

  propertyDescriptor = this.descriptorService.propertyDescriptor;
  isLoadingDescriptor = this.descriptorService.isLoadingDescriptor;

  private _canEdit = signal<boolean>(true);
  canEdit = this._canEdit.asReadonly();

  private _canDecrypt = signal<boolean>(false);
  canDecrypt = this._canDecrypt.asReadonly();

  private _errorMessage = signal<string | null>(null);
  errorMessage = this._errorMessage.asReadonly();

  saveDescriptor = output<PropertyDescriptor>();

  form: FormGroup<PropertyDescriptorForm>;
  propertyTypes = this.propertyTypesService.propertyTypes;
  selectedPropertyType = signal<PropertyType | null>(null);
  tags = signal<PropertyTag[]>([]);
  showForceDelete = signal<boolean>(false);

  isNewMode = computed(() => !this.propertyDescriptor()?.id);
  title = computed(() => (this.isNewMode() ? 'New Property Descriptor' : 'Edit Property Descriptor'));

  isLongDefaultValue = computed(() => {
    const val = this.form.controls.defaultValue.value;
    return val && val.length > 70;
  });

  isLongExampleValue = computed(() => {
    const val = this.form.controls.exampleValue.value;
    return val && val.length > 70;
  });

  isLongMik = computed(() => {
    const val = this.form.controls.mik.value;
    return val && val.length > 70;
  });

  constructor() {
    effect(() => {
      const id = this._descriptorId();
      if (id && id > 0) {
        this.descriptorService.loadPropertyDescriptor(id);
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

    effect(() => {
      const property = this.propertyDescriptor();
      if (property) {
        this.form.patchValue({
          name: property.name,
          displayName: property.displayName || '',
          validationRegex: property.validationRegex,
          nullable: property.nullable,
          optional: property.optional,
          encrypted: property.encrypted,
          mik: property.mik || '',
          defaultValue: property.defaultValue || '',
          exampleValue: property.exampleValue || '',
          comment: property.comment || '',
        });
        this.selectedPropertyType.set(property.propertyTypeEntity);
        this.tags.set([...property.propertyTags]);
      }
    });

    effect(() => {
      const error = this._errorMessage();
      if (error && error.includes('marked to be deleted')) {
        this.showForceDelete.set(true);
      }
    });

    effect(
      () => {
        if (!this._canEdit()) {
          this.form.disable();
        } else {
          this.form.enable();
          if (!this._canDecrypt()) {
            this.form.controls.encrypted.disable();
          }
        }
      },
      { allowSignalWrites: true },
    );
  }

  // Setter methods for modal component instance assignment
  set descriptorIdInput(value: number | null) {
    this._descriptorId.set(value);
  }

  set canEditInput(value: boolean) {
    this._canEdit.set(value);
  }

  set canDecryptInput(value: boolean) {
    this._canDecrypt.set(value);
  }

  set errorMessageInput(value: string | null) {
    this._errorMessage.set(value);
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
    if (this.form.valid && this.selectedPropertyType()) {
      const descriptor: PropertyDescriptor = {
        ...this.form.getRawValue(),
        propertyTypeEntity: this.selectedPropertyType()!,
        propertyTags: this.tags(),
        id: this.propertyDescriptor()?.id,
      };
      this.saveDescriptor.emit(descriptor);
    }
  }
}
