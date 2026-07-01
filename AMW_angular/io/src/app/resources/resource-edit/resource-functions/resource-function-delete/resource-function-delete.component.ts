import { Component, EventEmitter, inject, Input, Output, signal } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { ModalHeaderComponent } from '../../../../shared/modal-header/modal-header.component';
import { ButtonComponent } from '../../../../shared/button/button.component';

@Component({
  selector: 'app-resource-function-delete',
  standalone: true,
  templateUrl: './resource-function-delete.component.html',
  imports: [ModalHeaderComponent, ButtonComponent],
})
export class ResourceFunctionDeleteComponent {
  activeModal = inject(NgbActiveModal);

  private readonly functionIdSignal = signal<number | null>(null);

  // ng-bootstrap modal inputs are assigned through componentInstance; keep setter-backed signals until
  // https://github.com/ng-bootstrap/ng-bootstrap/issues/4664 is resolved.
  @Input({ required: true })
  set functionId(value: number) {
    this.functionIdSignal.set(value);
  }

  get functionId(): number {
    return this.functionIdSignal() as number;
  }
  @Output() deleteFunctionId: EventEmitter<number> = new EventEmitter<number>();

  cancel() {
    this.activeModal.close();
  }

  delete() {
    this.deleteFunctionId.emit(this.functionId);
    this.activeModal.close();
  }
}
