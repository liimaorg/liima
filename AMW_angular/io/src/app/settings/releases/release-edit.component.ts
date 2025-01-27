import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
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
  standalone: true,
  imports: [DatePickerComponent, FormsModule, ModalHeaderComponent, ButtonComponent],
})
export class ReleaseEditComponent implements OnInit {
  @Input() release: Release;
  @Output() saveRelease: EventEmitter<Release> = new EventEmitter<Release>();

  dateFormat = DATE_FORMAT;
  installationDate: DateModel = null;

  constructor(public activeModal: NgbActiveModal) {
    this.activeModal = activeModal;
  }

  ngOnInit(): void {
    if (this.release) {
      this.installationDate = DateModel.fromLocalString(this.release.installationInProductionAt);
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
    if (this.installationDate.toEpoch() != null) {
      const release: Release = {
        name: this.release.name,
        mainRelease: this.release.mainRelease,
        description: this.release.description,
        installationInProductionAt: this.installationDate.toISOFormat(),
        id: this.release.id ? this.release.id : null,
        default: false,
        v: this.release.v ? this.release.v : null,
      };
      this.saveRelease.emit(release);
      this.activeModal.close();
    }
  }
}
