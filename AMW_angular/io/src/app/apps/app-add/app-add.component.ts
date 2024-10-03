import { Component, EventEmitter, Input, Output, Signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NgSelectModule } from '@ng-select/ng-select';
import { Release } from '../../settings/releases/release';
import { Release as Rel } from '../../resource/release';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { Resource } from '../../resource/resource';
import { AppCreate } from '../app-create';

@Component({
  selector: 'amw-app-add',
  standalone: true,
  imports: [FormsModule, NgSelectModule],
  templateUrl: './app-add.component.html',
})
export class AppAddComponent {
  @Input() releases: Signal<Release[]>;
  @Input() appServerGroups: Signal<Resource[]>;
  @Output() saveApp: EventEmitter<AppCreate> = new EventEmitter<AppCreate>();

  app: AppCreate = { name: '', releaseId: null, appServerGroupId: null, appServerReleaseId: null };
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
      this.app.name === '' ||
      this.app.releaseId === null ||
      (!this.hasInvalidGroup() && (this.appServerRelease === undefined || this.appServerRelease === null))
    );
  }

  cancel() {
    this.activeModal.close();
  }

  save() {
    const app: AppCreate = {
      name: this.app.name,
      releaseId: this.app.releaseId,
      appServerGroupId: this.appServerGroup?.id,
      appServerReleaseId: this.appServerRelease?.id,
    };
    this.saveApp.emit(app);
    this.activeModal.close();
  }
}
