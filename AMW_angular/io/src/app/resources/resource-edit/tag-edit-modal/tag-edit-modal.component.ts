import { Component, EventEmitter, inject, Input, Output, signal } from '@angular/core';
import { NgbActiveModal, NgbDatepickerModule, NgbDateStruct } from '@ng-bootstrap/ng-bootstrap';
import { FormsModule } from '@angular/forms';
import { ModalHeaderComponent } from '../../../shared/modal-header/modal-header.component';
import { ButtonComponent } from '../../../shared/button/button.component';
import { Resource } from '../../models/resource';

export interface TagData {
  label: string;
  tagDate: Date;
}

@Component({
  selector: 'app-tag-edit-modal',
  standalone: true,
  imports: [FormsModule, ModalHeaderComponent, ButtonComponent, NgbDatepickerModule],
  templateUrl: './tag-edit-modal.component.html',
})
export class TagEditModalComponent {
  activeModal = inject(NgbActiveModal);

  @Input() resource!: Resource;
  @Output() saveTag = new EventEmitter<TagData>();

  tagLabel = signal<string>('');
  tagDate = signal<NgbDateStruct>(this.dateToNgbDate(new Date()));
  isCreatingTag = signal(false);

  getTitle(): string {
    return 'Tag Application Server';
  }

  cancel() {
    this.activeModal.close();
  }

  save() {
    const label = this.tagLabel();
    const ngbDate = this.tagDate();

    if (!label || label.trim() === '') {
      return;
    }

    if (!ngbDate) {
      return;
    }

    const date = this.ngbDateToDate(ngbDate);
    this.saveTag.emit({
      label: label.trim(),
      tagDate: date,
    });
    this.activeModal.close();
  }

  private dateToNgbDate(date: Date): NgbDateStruct {
    return {
      year: date.getFullYear(),
      month: date.getMonth() + 1,
      day: date.getDate(),
    };
  }

  private ngbDateToDate(ngbDate: NgbDateStruct): Date {
    return new Date(ngbDate.year, ngbDate.month - 1, ngbDate.day, 12, 0, 0);
  }
}
