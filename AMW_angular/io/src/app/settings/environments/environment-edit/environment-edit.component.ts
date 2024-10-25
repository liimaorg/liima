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
  @Output() saveEnvironment: EventEmitter<Environment> = new EventEmitter<Environment>();

  getTitle(): string {
    if (!this.environment) return;
    return this.environment.id ? 'Edit environment' : 'Add environment';
  }

  isEdit(): boolean {
    if (!this.environment) return;
    return !!this.environment.id;
  }

  cancel() {
    this.activeModal.close();
  }

  save() {
    const environment: Environment = {
      id: this.environment.id,
      name: this.environment.name,
      nameAlias: this.environment.nameAlias,
      parentName: this.environment.parentName,
      parentId: this.environment.parentId,
      selected: this.environment.selected,
      disabled: this.environment.disabled,
    };
    this.saveEnvironment.emit(environment);
    this.activeModal.close();
  }
}
