import { Component, EventEmitter, Input, Output, inject, signal } from '@angular/core';
import { Release } from '../../settings/releases/release';
import { AppServer } from '../app-server';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { FormsModule } from '@angular/forms';
import { NgSelectModule } from '@ng-select/ng-select';
import { ModalHeaderComponent } from '../../shared/modal-header/modal-header.component';
import { ButtonComponent } from '../../shared/button/button.component';

type AppServerForm = Omit<AppServer, 'id' | 'release'> & {
  id: number | null;
  release: Release | null;
};

@Component({
  selector: 'app-server-add',
  imports: [FormsModule, NgSelectModule, ModalHeaderComponent, ButtonComponent],
  templateUrl: './app-server-add.component.html',
})
export class AppServerAddComponent {
  activeModal = inject(NgbActiveModal);

  private readonly releasesSignal = signal<Release[]>([]);

  // ng-bootstrap modal inputs are assigned through componentInstance; keep setter-backed signals until
  // https://github.com/ng-bootstrap/ng-bootstrap/issues/4664 is resolved.
  @Input({ required: true })
  set releases(value: Release[]) {
    this.releasesSignal.set(value);
  }

  protected readonly releasesValue = this.releasesSignal.asReadonly();
  @Output() saveAppServer: EventEmitter<AppServer> = new EventEmitter<AppServer>();

  appServer: AppServerForm = { name: '', apps: [], deletable: false, id: null, runtimeName: '', release: null };

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

    const release = this.appServer.release;
    if (!release) {
      return;
    }

    const appServer: AppServer = {
      name: this.appServer.name,
      release,
      deletable: this.appServer.deletable,
      id: this.appServer.id ?? 0,
      runtimeName: this.appServer.runtimeName,
      apps: this.appServer.apps,
    };
    this.saveAppServer.emit(appServer);
    this.activeModal.close();
  }
}
