import {
  ChangeDetectionStrategy,
  Component,
  computed,
  EventEmitter,
  inject,
  Input,
  Output,
  signal,
} from '@angular/core';
import { Environment } from '../../../deployment/environment';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { FormsModule } from '@angular/forms';
import { ModalHeaderComponent } from '../../../shared/modal-header/modal-header.component';
import { AuthService } from '../../../auth/auth.service';
import { ButtonComponent } from '../../../shared/button/button.component';

@Component({
  selector: 'app-environment-edit',
  imports: [FormsModule, ModalHeaderComponent, ButtonComponent],
  templateUrl: './environment-edit.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class EnvironmentEditComponent {
  private authService = inject(AuthService);
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
  @Output() saveEnvironment: EventEmitter<Environment> = new EventEmitter<Environment>();

  permissions = computed(() => {
    if (this.authService.restrictions().length > 0) {
      return { canSave: this.authService.hasPermission('SAVE_SETTINGS_ENV', 'ALL') };
    } else {
      return { canSave: false };
    }
  });

  getTitle(): string | undefined {
    if (!this.environment) return;
    return this.environment.id
      ? `Edit ${this.isDomain() ? 'domain' : 'environment'} ${this.environment.name}`
      : `Add ${this.isDomain() ? 'domain' : 'environment'}`;
  }

  isDomain(): boolean {
    if (!this.environment) return false;
    return this.environment.parentName === this.globalName;
  }

  cancel() {
    this.activeModal.close();
  }

  save() {
    const forms: NodeListOf<Element> = document.querySelectorAll('.needs-validation');
    if (this.isValidForm()) {
      this.saveEnvironment.emit(this.environment);
      this.activeModal.close();
    } else {
      forms[0].classList.add('was-validated');
    }
  }

  isValidForm() {
    return this.environment.name.trim().length !== 0;
  }
}
