import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { NgFor, NgIf } from '@angular/common';
import { NgSelectModule } from '@ng-select/ng-select';
import { FormsModule } from '@angular/forms';
import { IconComponent } from '../../shared/icon/icon.component';
import { Release } from './release';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { DatePickerComponent } from '../../shared/date-picker/date-picker.component';
import { DATE_FORMAT } from '../../core/amw-constants';
import { DateModel } from '../../shared/date-picker/date.model';

@Component({
  selector: 'amw-release-edit',
  templateUrl: './release-edit.component.html',
  styleUrl: './release-edit.component.scss',
  standalone: true,
  imports: [DatePickerComponent, NgIf, NgSelectModule, FormsModule, NgFor, IconComponent],
})
export class ReleaseEditComponent implements OnInit {
  @Input() release: Release;

  @Output() cancelEdit: EventEmitter<boolean> = new EventEmitter<boolean>();
  @Output() saveRelease: EventEmitter<Release> = new EventEmitter<Release>();

  dateFormat = DATE_FORMAT;
  installationDate: DateModel = null;

  constructor(public activeModal: NgbActiveModal) {
    this.activeModal = activeModal;
  }

  ngOnInit(): void {
    if (this.release) {
      this.installationDate = DateModel.fromEpoch(this.release.installationInProductionAt);
    }
  }

  getTitle(): string {
    return 'Add release';
  }

  cancel() {
    this.activeModal.close();
  }

  save() {
    if (this.installationDate) {
      this.release.installationInProductionAt = this.installationDate.toEpoch();
    }

    const release: Release = {
      name: this.release.name,
      mainRelease: this.release.mainRelease,
      description: this.release.description,
      installationInProductionAt: this.release.installationInProductionAt,
      id: this.release.id,
      default: false,
    };
    this.saveRelease.emit(release);
  }
}
