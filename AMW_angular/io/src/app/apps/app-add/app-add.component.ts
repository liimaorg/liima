import { Component, EventEmitter, Input, Output, Signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NgSelectModule } from '@ng-select/ng-select';
import { Release } from '../../settings/releases/release';
import { Release as Rel } from '../../resource/release';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { Resource } from '../../resource/resource';
import { AppCreate } from '../app-create';
import { ModalHeaderComponent } from '../../shared/modal-header/modal-header.component';
import { ButtonComponent } from '../../shared/button/button.component';

@Component({
    selector: 'app-app-add',
    imports: [FormsModule, NgSelectModule, ModalHeaderComponent, ButtonComponent],
    templateUrl: './app-add.component.html'
})
export class AppAddComponent {
  @Input() releases: Signal<Release[]>;
  @Input() appServerGroups: Signal<Resource[]>;
  @Output() saveApp: EventEmitter<AppCreate> = new EventEmitter<AppCreate>();

  app: AppCreate = { appName: '', appReleaseId: null, appServerId: null, appServerReleaseId: null };
  appServerGroup: Resource;
  appServerRelease: Rel;

  constructor(public activeModal: NgbActiveModal) {
    this.activeModal = activeModal;
  }

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
      appServerId: this.appServerGroup?.id,
      appServerReleaseId: this.appServerRelease?.id,
    };
    this.saveApp.emit(app);
    this.activeModal.close();
  }
}
