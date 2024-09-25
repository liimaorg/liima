import { Component, EventEmitter, Input, Output } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { Function } from './function';

@Component({
  selector: 'amw-function-edit',
  templateUrl: './function-edit.component.html',
  standalone: true,
  imports: [FormsModule],
})
export class FunctionEditComponent {
  @Input() function: Function;
  @Output() saveFunction: EventEmitter<Function> = new EventEmitter<Function>();

  constructor(public activeModal: NgbActiveModal) {}

  getTitle(): string {
    return this.function.id ? 'Edit function' : 'Add function';
  }

  cancel() {
    this.activeModal.close();
  }

  save() {
    this.saveFunction.emit(this.function);
    this.activeModal.close();
  }
}