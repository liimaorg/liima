import { Component, inject, input, signal, TemplateRef, ViewChild } from '@angular/core';
import { NgbModal, NgbDatepickerModule, NgbDateStruct } from '@ng-bootstrap/ng-bootstrap';
import { FormsModule } from '@angular/forms';
import { ModalHeaderComponent } from '../../../shared/modal-header/modal-header.component';
import { ButtonComponent } from '../../../shared/button/button.component';
import { ResourceTagsService } from '../../services/resource-tags.service';
import { ToastService } from '../../../shared/elements/toast/toast.service';
import { Resource } from '../../models/resource';

@Component({
  selector: 'app-tag-current-state',
  standalone: true,
  imports: [FormsModule, ModalHeaderComponent, ButtonComponent, NgbDatepickerModule],
  templateUrl: './tag-current-state.component.html',
})
export class TagCurrentStateComponent {
  private modalService = inject(NgbModal);
  private resourceTagsService = inject(ResourceTagsService);
  private toastService = inject(ToastService);

  @ViewChild('tagModal') tagModal!: TemplateRef<void>;

  resource = input.required<Resource>();
  tagLabel = signal<string>('');
  tagDate = signal<NgbDateStruct>(this.dateToNgbDate(new Date()));
  isCreatingTag = signal(false);

  openTagDialog() {
    const now = new Date();
    this.tagLabel.set('');
    this.tagDate.set(this.dateToNgbDate(now));
    const modalRef = this.modalService.open(this.tagModal);
    modalRef.result.then(
      () => {
        this.createTag();
      },
      () => {
        this.tagLabel.set('');
        this.tagDate.set(this.dateToNgbDate(new Date()));
      },
    );
  }

  createTag() {
    const label = this.tagLabel();
    const ngbDate = this.tagDate();

    if (!label || label.trim() === '') {
      this.toastService.error('Tag label must not be empty.');
      return;
    }

    if (!ngbDate) {
      this.toastService.error('Tag date must not be empty.');
      return;
    }

    const date = this.ngbDateToDate(ngbDate);

    this.isCreatingTag.set(true);
    this.resourceTagsService
      .createTag(this.resource().id, {
        label: label.trim(),
        tagDate: date,
      })
      .subscribe({
        next: () => {
          this.toastService.success(`New tag '${label}' created.`);
          this.isCreatingTag.set(false);
          const now = new Date();
          this.tagLabel.set('');
          this.tagDate.set(this.dateToNgbDate(now));
        },
        error: (error) => {
          console.error('Failed to create tag:', error);
          const errorMessage = error?.error?.message || 'Failed to create tag.';
          this.toastService.error(errorMessage);
          this.isCreatingTag.set(false);
        },
      });
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
