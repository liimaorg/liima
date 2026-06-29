import { Component, EventEmitter, Input, Output, inject, signal } from '@angular/core';
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

  private readonly propertyTypeSignal = signal<PropertyType | null>(null);

  // ng-bootstrap modal inputs are assigned through componentInstance; keep setter-backed signals until
  // https://github.com/ng-bootstrap/ng-bootstrap/issues/4664 is resolved.
  @Input({ required: true })
  set propertyType(value: PropertyType) {
    this.propertyTypeSignal.set(value);
  }

  get propertyType(): PropertyType {
    return this.propertyTypeSignal() as PropertyType;
  }
  @Output() deletePropertyType: EventEmitter<PropertyType> = new EventEmitter<PropertyType>();

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
