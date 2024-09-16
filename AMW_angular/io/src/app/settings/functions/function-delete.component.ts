import { Component, EventEmitter, Input, Output } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { Function } from './function';

@Component({
  selector: 'amw-function-delete',
  standalone: true,
  templateUrl: './function-delete.component.html',
})
export class FunctionDeleteComponent {
  @Input() function: Function;
  @Output() deleteFunction: EventEmitter<Function> = new EventEmitter<Function>();

  constructor(public activeModal: NgbActiveModal) {}

  cancel() {
    this.activeModal.close();
  }

  delete() {
    this.deleteFunction.emit(this.function);
    this.activeModal.close();
  }
}
