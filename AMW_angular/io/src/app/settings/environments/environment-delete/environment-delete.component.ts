import { Component, EventEmitter, inject, Input, Output } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { Environment } from '../../../deployment/environment';
import { ModalHeaderComponent } from '../../../shared/modal-header/modal-header.component';
import { ButtonComponent } from '../../../shared/button/button.component';

@Component({
  selector: 'app-environment-delete',
  templateUrl: './environment-delete.component.html',
  imports: [ModalHeaderComponent, ButtonComponent],
})
export class EnvironmentDeleteComponent {
  activeModal = inject(NgbActiveModal);
  @Input() environment: Environment;
  @Input() globalName: string;
  @Output() deleteEnvironment: EventEmitter<Environment> = new EventEmitter<Environment>();

  cancel() {
    this.activeModal.close();
  }

  delete() {
    this.deleteEnvironment.emit(this.environment);
    this.activeModal.close();
  }

  getContextType() {
    if (!this.environment) return;
    return this.environment.parentName === this.globalName ? 'domain' : 'environment';
  }
}
