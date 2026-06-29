import { Component, EventEmitter, inject, Input, Output, signal } from '@angular/core';
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

  private readonly environmentSignal = signal<Environment | null>(null);
  private readonly globalNameSignal = signal<string>('');

  // ng-bootstrap modal inputs are assigned through componentInstance; keep setter-backed signals until
  // https://github.com/ng-bootstrap/ng-bootstrap/issues/4664 is resolved.
  @Input({ required: true })
  set environment(value: Environment) {
    this.environmentSignal.set(value);
  }

  get environment(): Environment {
    return this.environmentSignal() as Environment;
  }

  @Input({ required: true })
  set globalName(value: string) {
    this.globalNameSignal.set(value);
  }

  get globalName(): string {
    return this.globalNameSignal();
  }
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
