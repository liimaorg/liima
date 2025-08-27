import { Component, EventEmitter, Input, Output, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { PropertyType } from './property-type';
import { IconComponent } from '../../shared/icon/icon.component';
import { PropertyTag } from './property-tag';
import { ModalHeaderComponent } from '../../shared/modal-header/modal-header.component';
import { ButtonComponent } from '../../shared/button/button.component';

@Component({
  selector: 'app-property-type-edit',
  templateUrl: './property-type-edit.component.html',
  imports: [IconComponent, FormsModule, ModalHeaderComponent, ButtonComponent],
})
export class PropertyTypeEditComponent {
  activeModal = inject(NgbActiveModal);

  @Input() propertyType: PropertyType;
  @Output() savePropertyType: EventEmitter<PropertyType> = new EventEmitter<PropertyType>();

  title = 'property type';
  newTag: string = '';

  constructor() {
    const activeModal = this.activeModal;
    this.activeModal = activeModal;
  }

  getTitle(): string {
    return this.propertyType.id ? `Edit ${this.title}` : `Add ${this.title}`;
  }

  cancel() {
    this.activeModal.close();
  }

  isValidRegex() {
    if (this.propertyType.validationRegex === '') {
      return true;
    }
    try {
      ''.match(this.propertyType.validationRegex);
      return true;
    } catch {
      return false;
    }
  }

  isValidForm() {
    return this.propertyType.name !== '' && this.propertyType.validationRegex !== '';
  }

  save() {
    const propertyType: PropertyType = {
      name: this.propertyType.name,
      id: this.propertyType.id ? this.propertyType.id : null,
      validationRegex: this.propertyType.validationRegex,
      encrypted: this.propertyType.encrypted,
      propertyTags: this.propertyType.propertyTags,
    };
    this.savePropertyType.emit(propertyType);
    this.activeModal.close();
  }

  deleteTag(tag: PropertyTag) {
    this.propertyType.propertyTags = this.propertyType.propertyTags.filter((value) => {
      return value.type !== tag.type || value.name !== tag.name;
    });
  }

  addTag() {
    const tag = this.newTag.trim();
    if (tag !== '') {
      this.propertyType.propertyTags.push({ name: tag, type: 'LOCAL' });
    }
    this.newTag = '';
  }
}
