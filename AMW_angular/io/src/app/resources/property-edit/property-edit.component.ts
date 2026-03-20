import { ChangeDetectionStrategy, Component, computed, effect, inject, input, output, signal } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { ButtonComponent } from '../../shared/button/button.component';
import { ModalHeaderComponent } from '../../shared/modal-header/modal-header.component';
import { NgOptionComponent, NgSelectComponent } from '@ng-select/ng-select';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { PropertyType } from '../../settings/property-types/property-type';
import { PropertyTag } from '../../settings/property-types/property-tag';
import { CommonModule } from '@angular/common';
import { PropertyDescriptor } from '../models/property-descriptor';
import { PropertyTypesService } from '../../settings/property-types/property-types.service';

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
  ],
  templateUrl: './property-edit.component.html',
  styleUrl: './property-edit.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PropertyEditComponent {
  activeModal = inject(NgbActiveModal);
  private propertyTypesService = inject(PropertyTypesService);

  // TODO load propertyDescriptorEnity from backend
  propertyDescriptor = input<PropertyDescriptor | null>(null);
  canEdit = input<boolean>(true);
  canDecrypt = input<boolean>(false);
  errorMessage = input<string | null>(null);

  saveDescriptor = output<PropertyDescriptor>();
  deleteDescriptor = output<number>();
  forceDeleteDescriptor = output<number>();

  form: FormGroup<PropertyDescriptorForm>;
  propertyTypes = this.propertyTypesService.propertyTypes;
  selectedPropertyType = signal<PropertyType | null>(null);
  tags = signal<PropertyTag[]>([]);
  newTagInput = signal<string>('');
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
    this.form = new FormGroup<PropertyDescriptorForm>({
      name: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
      displayName: new FormControl('', { nonNullable: true }),
      validationRegex: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
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
      const error = this.errorMessage();
      if (error && error.includes('marked to be deleted')) {
        this.showForceDelete.set(true);
      }
    });

    effect(
      () => {
        if (!this.canEdit()) {
          this.form.disable();
        } else {
          this.form.enable();
          if (!this.canDecrypt()) {
            this.form.controls.encrypted.disable();
          }
        }
      },
      { allowSignalWrites: true },
    );
  }

  onPropertyTypeChange(typeId: number) {
    const selectedType = this.propertyTypes().find((t) => t.id === typeId);
    if (selectedType) {
      this.selectedPropertyType.set(selectedType);
      this.form.controls.validationRegex.setValue(selectedType.validationRegex);
      this.form.controls.encrypted.setValue(selectedType.encrypted);
    }
  }

  addTag() {
    const tagName = this.newTagInput().trim();
    if (tagName && !this.tags().some((t) => t.name === tagName)) {
      this.tags.update((tags) => [...tags, { name: tagName, type: 'custom' }]);
      this.newTagInput.set('');
    }
  }

  removeTag(tagName: string) {
    this.tags.update((tags) => tags.filter((t) => t.name !== tagName));
  }

  onTagInputKeydown(event: KeyboardEvent) {
    if (event.key === 'Enter') {
      event.preventDefault();
      this.addTag();
    }
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

  delete() {
    const id = this.propertyDescriptor()?.id;
    if (id) {
      this.deleteDescriptor.emit(id);
    }
  }

  forceDelete() {
    const id = this.propertyDescriptor()?.id;
    if (id) {
      this.forceDeleteDescriptor.emit(id);
    }
  }
}
