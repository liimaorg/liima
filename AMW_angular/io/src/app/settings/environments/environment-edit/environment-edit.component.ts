import { ChangeDetectionStrategy, Component, computed, EventEmitter, inject, Input, Output } from '@angular/core';
import { Environment } from '../../../deployment/environment';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { FormsModule } from '@angular/forms';
import { ModalHeaderComponent } from '../../../shared/modal-header/modal-header.component';
import { AuthService } from '../../../auth/auth.service';
import { ButtonComponent } from '../../../shared/button/button.component';
import { IconComponent } from '../../../shared/icon/icon.component';

@Component({
  selector: 'app-environment-edit',
  standalone: true,
  imports: [FormsModule, ModalHeaderComponent, ButtonComponent, IconComponent],
  templateUrl: './environment-edit.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class EnvironmentEditComponent {
  private authService = inject(AuthService);
  activeModal = inject(NgbActiveModal);
  @Input() environment: Environment;
  @Input() globalName: string;
  @Output() saveEnvironment: EventEmitter<Environment> = new EventEmitter<Environment>();

  canSave: boolean = false;

  loadingPermissions = computed(() => {
    if (this.authService.restrictions().length > 0) {
      this.getUserPermissions();
    } else {
      return `<div>Could not load permissions</div>`;
    }
  });

  private getUserPermissions() {
    this.canSave = this.authService.hasPermission('SAVE_SETTINGS_ENV', 'ALL');
  }

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
