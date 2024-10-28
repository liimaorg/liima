import { Component, EventEmitter, inject, Input, Output } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { Environment } from '../../../deployment/environment';

@Component({
  selector: 'app-function-delete',
  standalone: true,
  templateUrl: './environment-delete.component.html',
})
export class EnvironmentDeleteComponent {
  activeModal = inject(NgbActiveModal);
  @Input() environment: Environment;
  @Output() deleteEnvironment: EventEmitter<Environment> = new EventEmitter<Environment>();

  cancel() {
    this.activeModal.close();
  }

  delete() {
    this.deleteEnvironment.emit(this.environment);
    this.activeModal.close();
  }
}
