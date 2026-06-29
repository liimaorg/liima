import { Component, EventEmitter, inject, Input, Output, signal } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { ResourceType } from '../models/resource-type';
import { ModalHeaderComponent } from '../../shared/modal-header/modal-header.component';
import { ButtonComponent } from '../../shared/button/button.component';

@Component({
  selector: 'app-resource-type-delete',
  templateUrl: './resource-type-delete.component.html',
  imports: [ModalHeaderComponent, ButtonComponent, ModalHeaderComponent, ButtonComponent],
})
export class ResourceTypeDeleteComponent {
  activeModal = inject(NgbActiveModal);

  private readonly resourceTypeSignal = signal<ResourceType | null>(null);

  // ng-bootstrap modal inputs are assigned through componentInstance; keep setter-backed signals until
  // https://github.com/ng-bootstrap/ng-bootstrap/issues/4664 is resolved.
  @Input({ required: true })
  set resourceType(value: ResourceType) {
    this.resourceTypeSignal.set(value);
  }

  get resourceType(): ResourceType {
    return this.resourceTypeSignal() as ResourceType;
  }
  @Output() resourceTypeToDelete: EventEmitter<ResourceType> = new EventEmitter<ResourceType>();

  cancel() {
    this.activeModal.close();
  }

  delete() {
    this.resourceTypeToDelete.emit(this.resourceType);
    this.activeModal.close();
  }
}
