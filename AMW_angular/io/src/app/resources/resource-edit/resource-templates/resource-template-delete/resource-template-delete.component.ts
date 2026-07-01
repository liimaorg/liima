import { Component, EventEmitter, inject, Input, Output, signal } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { ModalHeaderComponent } from '../../../../shared/modal-header/modal-header.component';
import { ButtonComponent } from '../../../../shared/button/button.component';

@Component({
  selector: 'app-resource-template-delete',
  standalone: true,
  templateUrl: './resource-template-delete.component.html',
  imports: [ModalHeaderComponent, ButtonComponent],
})
export class ResourceTemplateDeleteComponent {
  activeModal = inject(NgbActiveModal);

  private readonly templateIdSignal = signal<number | null>(null);

  // ng-bootstrap modal inputs are assigned through componentInstance; keep setter-backed signals until
  // https://github.com/ng-bootstrap/ng-bootstrap/issues/4664 is resolved.
  @Input({ required: true })
  set templateId(value: number) {
    this.templateIdSignal.set(value);
  }

  get templateId(): number {
    return this.templateIdSignal() as number;
  }
  @Output() deleteTemplateId: EventEmitter<number> = new EventEmitter<number>();

  cancel() {
    this.activeModal.close();
  }

  delete() {
    this.deleteTemplateId.emit(this.templateId);
    this.activeModal.close();
  }
}
