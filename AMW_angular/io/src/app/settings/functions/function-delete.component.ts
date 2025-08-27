import { Component, EventEmitter, Input, Output, inject } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { AppFunction } from './appFunction';
import { ModalHeaderComponent } from '../../shared/modal-header/modal-header.component';
import { ButtonComponent } from '../../shared/button/button.component';

@Component({
  selector: 'app-function-delete',
  templateUrl: './function-delete.component.html',
  imports: [ModalHeaderComponent, ButtonComponent],
})
export class FunctionDeleteComponent {
  activeModal = inject(NgbActiveModal);

  @Input() function: AppFunction;
  @Output() deleteFunction: EventEmitter<AppFunction> = new EventEmitter<AppFunction>();

  cancel() {
    this.activeModal.close();
  }

  delete() {
    this.deleteFunction.emit(this.function);
    this.activeModal.close();
  }
}
