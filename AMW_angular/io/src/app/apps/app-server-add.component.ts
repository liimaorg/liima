import { Component, EventEmitter, Input, Output } from '@angular/core';
import { Release } from '../settings/releases/release';
import { AppServer } from './app-server';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'amw-app-server-add',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './app-server-add.component.html',
})
export class AppServerAddComponent {
  @Input() releases: Release[];
  @Output() saveAppServer: EventEmitter<AppServer> = new EventEmitter<AppServer>();

  appServer: AppServer = { name: '', apps: [], deletable: false, id: null, runtimeName: '', release: null };

  constructor(public activeModal: NgbActiveModal) {
    this.activeModal = activeModal;
  }

  cancel() {
    this.activeModal.close();
  }

  save() {
    const appServer: AppServer = {
      name: this.appServer.name,
      release: this.appServer.release,
      deletable: this.appServer.deletable,
      id: this.appServer.id,
      runtimeName: this.appServer.runtimeName,
      apps: this.appServer.apps,
    };
    this.saveAppServer.emit(appServer);
    this.activeModal.close();
  }
}
