import { Component, EventEmitter, Input, Output, Signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NgSelectModule } from '@ng-select/ng-select';
import { Release } from '../../settings/releases/release';
import { Release as Rel } from '../../resource/release';
import { AppServer } from '../app-server';
import { App } from '../app';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { Resource } from '../../resource/resource';

@Component({
  selector: 'amw-app-add',
  standalone: true,
  imports: [FormsModule, NgSelectModule],
  templateUrl: './app-add.component.html',
})
export class AppAddComponent {
  @Input() releases: Signal<Release[]>;
  @Input() appServerGroups: Signal<Resource[]>;
  @Output() saveApp: EventEmitter<App> = new EventEmitter<App>();

  app: App = { name: '', id: 1, release: null };
  //update appserver apps??
  appServerGroup: Resource;
  appServerRelease: Rel;

  constructor(public activeModal: NgbActiveModal) {
    this.activeModal = activeModal;
  }

  hasInvalidGroup(): boolean {
    return this.appServerGroup === undefined || this.appServerGroup.releases === null;
  }

  hasInvalidFields(): boolean {
    return this.app.name === '' || this.app.release === null || this.app.release.name === '';
  }

  cancel() {
    this.activeModal.close();
  }

  save() {
    const app: App = {
      name: this.app.name,
      release: this.app.release,
      id: null,
    };
    this.saveApp.emit(app);
    this.activeModal.close();
  }
}
