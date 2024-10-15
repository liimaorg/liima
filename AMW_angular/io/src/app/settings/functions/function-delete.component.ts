import { Component, EventEmitter, Input, Output } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { AppFunction } from './appFunction';

@Component({
  selector: 'amw-function-delete',
  standalone: true,
  templateUrl: './function-delete.component.html',
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
