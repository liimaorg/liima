import { ChangeDetectionStrategy, Component, computed, effect, inject, input, output, signal } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { ButtonComponent } from '../../shared/button/button.component';
import { ModalHeaderComponent } from '../../shared/modal-header/modal-header.component';
import { NgOptionComponent, NgSelectComponent } from '@ng-select/ng-select';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { PropertyType } from '../../settings/property-types/property-type';
import { PropertyTag } from '../../settings/property-types/property-tag';
import { CommonModule } from '@angular/common';
import { Property } from '../models/property';

interface PropertyDescriptorForm {
  propertyName: FormControl<string>;
  displayName: FormControl<string>;
  propertyTypeId: FormControl<number>;
  validationLogic: FormControl<string>;
  nullable: FormControl<boolean>;
  optional: FormControl<boolean>;
  encrypt: FormControl<boolean>;
  machineInterpretationKey: FormControl<string>;
  defaultValue: FormControl<string>;
  exampleValue: FormControl<string>;
  propertyComment: FormControl<string>;
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

  property = input<Property | null>(null);
  propertyTypes = input.required<PropertyType[]>();
  globalTags = input.required<PropertyTag[]>();
  canEdit = input<boolean>(true);
  canDecrypt = input<boolean>(false);
  errorMessage = input<string | null>(null);

  saveDescriptor = output<PropertyDescriptor>();
  deleteDescriptor = output<number>();
  forceDeleteDescriptor = output<number>();

  form: FormGroup<PropertyDescriptorForm>;
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
    const val = this.form.controls.machineInterpretationKey.value;
    return val && val.length > 70;
  });

  constructor() {
    this.form = new FormGroup<PropertyDescriptorForm>({
      propertyName: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
      displayName: new FormControl('', { nonNullable: true }),
      propertyTypeId: new FormControl(0, { nonNullable: true, validators: [Validators.required] }),
      validationLogic: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
      nullable: new FormControl(false, { nonNullable: true }),
      optional: new FormControl(false, { nonNullable: true }),
      encrypt: new FormControl(false, { nonNullable: true }),
      machineInterpretationKey: new FormControl('', { nonNullable: true }),
      defaultValue: new FormControl('', { nonNullable: true }),
      exampleValue: new FormControl('', { nonNullable: true }),
      propertyComment: new FormControl('', { nonNullable: true }),
    });

    effect(() => {
      const descriptor = this.propertyDescriptor();
      if (descriptor) {
        this.form.patchValue({
          propertyName: descriptor.propertyName,
          displayName: descriptor.displayName || '',
          propertyTypeId: descriptor.propertyTypeId,
          validationLogic: descriptor.validationLogic,
          nullable: descriptor.nullable,
          optional: descriptor.optional,
          encrypt: descriptor.encrypt,
          machineInterpretationKey: descriptor.machineInterpretationKey || '',
          defaultValue: descriptor.defaultValue || '',
          exampleValue: descriptor.exampleValue || '',
          propertyComment: descriptor.propertyComment || '',
        });
        this.tags.set([...descriptor.tags]);
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
            this.form.controls.encrypt.disable();
          }
        }
      },
      { allowSignalWrites: true },
    );
  }

  onPropertyTypeChange(typeId: number) {
    const selectedType = this.propertyTypes().find((t) => t.id === typeId);
    if (selectedType) {
      this.form.controls.validationLogic.setValue(selectedType.validationRegex);
      this.form.controls.encrypt.setValue(selectedType.encrypted);
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
    if (this.form.valid) {
      const descriptor: PropertyDescriptor = {
        ...this.form.getRawValue(),
        tags: this.tags(),
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
