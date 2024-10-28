import { ChangeDetectionStrategy, Component, EventEmitter, inject, Input, Output } from '@angular/core';
import { Environment } from '../../../deployment/environment';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-environment-edit',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './environment-edit.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class EnvironmentEditComponent {
  activeModal = inject(NgbActiveModal);
  @Input() environment: Environment;
  @Input() globalName: string;
  @Output() saveEnvironment: EventEmitter<Environment> = new EventEmitter<Environment>();

  getTitle(): string {
    if (!this.environment) return;
    return this.environment.id ? `Edit ${this.getContextType()}` : `Add ${this.getContextType()}`;
  }

  getContextType(): string {
    if (!this.environment) return;
    return this.environment.parentName === this.globalName ? 'domain' : 'environment';
  }

  isEdit(): boolean {
    if (!this.environment) return;
    return !!this.environment.id;
  }

  cancel() {
    this.activeModal.close();
  }

  save() {
    this.saveEnvironment.emit(this.environment);
    this.activeModal.close();
  }
}
