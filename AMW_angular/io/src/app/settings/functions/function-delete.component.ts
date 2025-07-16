import { Component, EventEmitter, Input, Output } from '@angular/core';
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
  @Input() function: AppFunction;
  @Output() deleteFunction: EventEmitter<AppFunction> = new EventEmitter<AppFunction>();

  constructor(public activeModal: NgbActiveModal) {}

  cancel() {
    this.activeModal.close();
  }

  delete() {
    this.deleteFunction.emit(this.function);
    this.activeModal.close();
  }
}
