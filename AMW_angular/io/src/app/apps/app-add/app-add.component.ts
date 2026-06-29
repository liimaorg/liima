import { Component, EventEmitter, Input, Output, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NgSelectModule } from '@ng-select/ng-select';
import { Release } from '../../settings/releases/release';
import { Release as Rel } from '../../resources/models/release';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { Resource } from '../../resources/models/resource';
import { AppCreate } from '../app-create';
import { ModalHeaderComponent } from '../../shared/modal-header/modal-header.component';
import { ButtonComponent } from '../../shared/button/button.component';

@Component({
  selector: 'app-app-add',
  imports: [FormsModule, NgSelectModule, ModalHeaderComponent, ButtonComponent],
  templateUrl: './app-add.component.html',
})
export class AppAddComponent {
  activeModal = inject(NgbActiveModal);

  private readonly releasesSignal = signal<Release[]>([]);
  private readonly appServerGroupsSignal = signal<Resource[]>([]);

  // ng-bootstrap modal inputs are assigned through componentInstance; keep setter-backed signals until
  // https://github.com/ng-bootstrap/ng-bootstrap/issues/4664 is resolved.
  @Input({ required: true })
  set releases(value: Release[]) {
    this.releasesSignal.set(value);
  }

  @Input({ required: true })
  set appServerGroups(value: Resource[]) {
    this.appServerGroupsSignal.set(value);
  }

  protected readonly releasesValue = this.releasesSignal.asReadonly();
  protected readonly appServerGroupsValue = this.appServerGroupsSignal.asReadonly();
  @Output() saveApp: EventEmitter<AppCreate> = new EventEmitter<AppCreate>();

  app: AppCreate = { appName: '', appReleaseId: null, appServerId: null, appServerReleaseId: null };
  appServerGroup: Resource | undefined = undefined;
  appServerRelease: Rel | undefined = undefined;

  hasInvalidGroup(): boolean {
    const isInvalid =
      this.appServerGroup === undefined || this.appServerGroup === null || this.appServerGroup?.releases.length === 0;
    if (isInvalid) {
      this.appServerRelease = undefined;
    }
    return isInvalid;
  }

  // apps without appserver are valid too
  hasInvalidFields(): boolean {
    return (
      this.app.appName === '' ||
      this.app.appReleaseId === null ||
      (!this.hasInvalidGroup() && (this.appServerRelease === undefined || this.appServerRelease === null))
    );
  }

  cancel() {
    this.activeModal.close();
  }

  save() {
    if (this.hasInvalidFields()) {
      return;
    }
    const app: AppCreate = {
      appName: this.app.appName,
      appReleaseId: this.app.appReleaseId,
      appServerId: this.appServerGroup?.id ?? null,
      appServerReleaseId: this.appServerRelease?.id ?? null,
    };
    this.saveApp.emit(app);
    this.activeModal.close();
  }
}
