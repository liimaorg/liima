import { Component, EventEmitter, Input, OnInit, Output, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Release } from './release';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { DatePickerComponent } from '../../shared/date-picker/date-picker.component';
import { DATE_FORMAT } from '../../core/amw-constants';
import { DateModel } from '../../shared/date-picker/date.model';
import { ModalHeaderComponent } from '../../shared/modal-header/modal-header.component';
import { ButtonComponent } from '../../shared/button/button.component';

@Component({
  selector: 'app-release-edit',
  templateUrl: './release-edit.component.html',
  imports: [DatePickerComponent, FormsModule, ModalHeaderComponent, ButtonComponent],
})
export class ReleaseEditComponent implements OnInit {
  activeModal = inject(NgbActiveModal);

  private readonly releaseSignal = signal<Release | null>(null);

  // ng-bootstrap modal inputs are assigned through componentInstance; keep setter-backed signals until
  // https://github.com/ng-bootstrap/ng-bootstrap/issues/4664 is resolved.
  @Input({ required: true })
  set release(value: Release) {
    this.releaseSignal.set(value);
  }

  get release(): Release {
    return this.releaseSignal() as Release;
  }
  @Output() saveRelease: EventEmitter<Release> = new EventEmitter<Release>();

  dateFormat = DATE_FORMAT;
  installationDate: DateModel | null = null;

  ngOnInit(): void {
    if (this.release) {
      this.installationDate = DateModel.fromEpoch(this.release.installationInProductionAt!);
    }
  }

  getTitle(): string {
    return this.release.id ? 'Edit release' : 'Add release';
  }

  hasInvalidDate(): boolean {
    return this.installationDate == null || this.installationDate.toEpoch() == null;
  }

  cancel() {
    this.activeModal.close();
  }

  save() {
    if (this.hasInvalidDate()) {
      return;
    }
    if (this.installationDate!.toEpoch() != null) {
      const release: Release = {
        name: this.release.name,
        mainRelease: this.release.mainRelease,
        description: this.release.description,
        installationInProductionAt: this.installationDate!.toEpoch(),
        id: this.release.id ? this.release.id : null,
        default: false,
        v: this.release.v ? this.release.v : null,
      };
      this.saveRelease.emit(release);
      this.activeModal.close();
    }
  }
}
