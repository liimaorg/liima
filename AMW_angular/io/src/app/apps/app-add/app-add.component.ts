import { Component, EventEmitter, Input, Output, Signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NgSelectModule } from '@ng-select/ng-select';
import { Release } from '../../settings/releases/release';
import { AppServer } from '../app-server';
import { App } from '../app';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';

@Component({
  selector: 'amw-app-add',
  standalone: true,
  imports: [FormsModule, NgSelectModule],
  templateUrl: './app-add.component.html',
})
export class AppAddComponent {
  @Input() releases: Signal<Release[]>;
  @Output() saveApp: EventEmitter<App> = new EventEmitter<App>();

  app: App = { name: '', id: null, release: null };
  //update appserver apps??
  appServer: AppServer = { name: '', apps: [], deletable: false, id: null, runtimeName: '', release: null };

  constructor(public activeModal: NgbActiveModal) {
    this.activeModal = activeModal;
  }

  hasInvalidFields(): boolean {
    return this.app.name === '' || this.app.release.id === null || this.app.release.name === '';
  }

  cancel() {
    this.activeModal.close();
  }

  save() {
    const app: App = {
      name: this.appServer.name,
      release: this.appServer.release,
      id: this.appServer.id,
    };
    this.saveApp.emit(app);
    this.activeModal.close();
  }
}
