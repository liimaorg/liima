import { Component, EventEmitter, Input, Output, inject } from '@angular/core';
import { PropertyType } from './property-type';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { FormsModule } from '@angular/forms';
import { ModalHeaderComponent } from '../../shared/modal-header/modal-header.component';
import { ButtonComponent } from '../../shared/button/button.component';

@Component({
  selector: 'app-property-type-delete',
  imports: [FormsModule, ModalHeaderComponent, ButtonComponent],
  templateUrl: './property-type-delete.component.html',
})
export class PropertyTypeDeleteComponent {
  activeModal = inject(NgbActiveModal);

  @Input() propertyType: PropertyType;
  @Output() deletePropertyType: EventEmitter<PropertyType> = new EventEmitter<PropertyType>();

  constructor() {
    const activeModal = this.activeModal;

    this.activeModal = activeModal;
  }

  getTitle(): string {
    return 'Remove property type';
  }

  cancel() {
    this.activeModal.close();
  }

  delete() {
    this.deletePropertyType.emit(this.propertyType);
    this.activeModal.close();
  }
}
