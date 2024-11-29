import { Component, EventEmitter, inject, Input, Output } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { ResourceType } from '../../resource/resource-type';
import { ModalHeaderComponent } from '../../shared/modal-header/modal-header.component';
import { ButtonComponent } from '../../shared/button/button.component';

@Component({
  selector: 'app-resource-type-delete',
  standalone: true,
  templateUrl: './resource-type-delete.component.html',
  imports: [ModalHeaderComponent, ButtonComponent, ModalHeaderComponent, ButtonComponent],
})
export class ResourceTypeDeleteComponent {
  activeModal = inject(NgbActiveModal);
  @Input() resourceType: ResourceType;
  @Output() resourceTypeToDelete: EventEmitter<ResourceType> = new EventEmitter<ResourceType>();

  cancel() {
    this.activeModal.close();
  }

  delete() {
    this.resourceTypeToDelete.emit(this.resourceType);
    this.activeModal.close();
  }
}
