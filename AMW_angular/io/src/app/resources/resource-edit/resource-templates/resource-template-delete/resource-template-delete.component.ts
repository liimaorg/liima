import { Component, EventEmitter, inject, Input, Output } from '@angular/core';
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
  @Input() templateId: number;
  @Output() deleteTemplateId: EventEmitter<number> = new EventEmitter<number>();

  cancel() {
    this.activeModal.close();
  }

  delete() {
    this.deleteTemplateId.emit(this.templateId);
    this.activeModal.close();
  }
}
