import { Component, EventEmitter, Input, Output, Signal } from '@angular/core';
import { Release } from '../../settings/releases/release';
import { AppServer } from '../app-server';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { FormsModule } from '@angular/forms';
import { NgSelectModule } from '@ng-select/ng-select';
import { ModalHeaderComponent } from '../../shared/modal-header/modal-header.component';
import { ButtonComponent } from '../../shared/button/button.component';

@Component({
  selector: 'app-server-add',
  standalone: true,
  imports: [FormsModule, NgSelectModule, ModalHeaderComponent, ButtonComponent],
  templateUrl: './app-server-add.component.html',
})
export class AppServerAddComponent {
  @Input() releases: Signal<Release[]>;
  @Output() saveAppServer: EventEmitter<AppServer> = new EventEmitter<AppServer>();

  appServer: AppServer = { name: '', apps: [], deletable: false, id: null, runtimeName: '', release: null };

  constructor(public activeModal: NgbActiveModal) {
    this.activeModal = activeModal;
  }

  hasInvalidFields(): boolean {
    return this.appServer.name === '' || this.appServer.release?.id == null;
  }

  cancel() {
    this.activeModal.close();
  }

  save() {
    if (this.hasInvalidFields()) {
      document.querySelectorAll('.needs-validation')[0].classList.add('was-validated');
      return;
    }
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
