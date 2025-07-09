import { Component, EventEmitter, inject, Input, Output } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { ModalHeaderComponent } from '../../../shared/modal-header/modal-header.component';
import { ButtonComponent } from '../../../shared/button/button.component';

@Component({
  selector: 'app-resource-function-delete',
  standalone: true,
  templateUrl: './resource-function-delete.component.html',
  imports: [ModalHeaderComponent, ButtonComponent],
})
export class ResourceFunctionDeleteComponent {
  activeModal = inject(NgbActiveModal);
  @Input() functionId: number;
  @Output() deleteFunctionId: EventEmitter<number> = new EventEmitter<number>();

  cancel() {
    this.activeModal.close();
  }

  delete() {
    this.deleteFunctionId.emit(this.functionId);
    this.activeModal.close();
  }
}
