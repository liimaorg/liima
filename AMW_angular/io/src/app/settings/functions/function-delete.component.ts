import { Component, EventEmitter, Input, Output, inject, signal } from '@angular/core';
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

  private readonly functionSignal = signal<AppFunction | null>(null);

  // ng-bootstrap modal inputs are assigned through componentInstance; keep setter-backed signals until
  // https://github.com/ng-bootstrap/ng-bootstrap/issues/4664 is resolved.
  @Input({ required: true })
  set function(value: AppFunction) {
    this.functionSignal.set(value);
  }

  get function(): AppFunction {
    return this.functionSignal() as AppFunction;
  }
  @Output() deleteFunction: EventEmitter<AppFunction> = new EventEmitter<AppFunction>();

  cancel() {
    this.activeModal.close();
  }

  delete() {
    this.deleteFunction.emit(this.function);
    this.activeModal.close();
  }
}
