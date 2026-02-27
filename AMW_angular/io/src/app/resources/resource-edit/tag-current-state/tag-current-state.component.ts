import { Component, inject, input, signal, TemplateRef, ViewChild } from '@angular/core';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { FormsModule } from '@angular/forms';
import { ModalHeaderComponent } from '../../../shared/modal-header/modal-header.component';
import { ButtonComponent } from '../../../shared/button/button.component';
import { ResourceTagsService } from '../../services/resource-tags.service';
import { ToastService } from '../../../shared/elements/toast/toast.service';
import { Resource } from '../../models/resource';

@Component({
  selector: 'app-tag-current-state',
  standalone: true,
  imports: [FormsModule, ModalHeaderComponent, ButtonComponent],
  templateUrl: './tag-current-state.component.html',
})
export class TagCurrentStateComponent {
  private modalService = inject(NgbModal);
  private resourceTagsService = inject(ResourceTagsService);
  private toastService = inject(ToastService);

  @ViewChild('tagModal') tagModal!: TemplateRef<void>;

  resource = input.required<Resource>();

  tagLabel = signal<string>('');
  tagDate = signal<Date>(new Date());
  isCreatingTag = signal(false);

  openTagDialog() {
    this.tagLabel.set('');
    this.tagDate.set(new Date());
    const modalRef = this.modalService.open(this.tagModal);
    modalRef.result.then(
      () => {
        this.createTag();
      },
      () => {
        this.tagLabel.set('');
        this.tagDate.set(new Date());
      },
    );
  }

  createTag() {
    const label = this.tagLabel();
    const date = this.tagDate();

    if (!label || label.trim() === '') {
      this.toastService.error('Tag label must not be empty.');
      return;
    }

    if (!date) {
      this.toastService.error('Tag date must not be empty.');
      return;
    }

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
          this.tagLabel.set('');
          this.tagDate.set(new Date());
        },
        error: (error) => {
          console.error('Failed to create tag:', error);
          const errorMessage = error?.error?.message || 'Failed to create tag.';
          this.toastService.error(errorMessage);
          this.isCreatingTag.set(false);
        },
      });
  }

  formatDateForInput(date: Date): string {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    const hours = String(date.getHours()).padStart(2, '0');
    const minutes = String(date.getMinutes()).padStart(2, '0');
    return `${year}-${month}-${day}T${hours}:${minutes}`;
  }

  onDateChange(event: Event) {
    const input = event.target as HTMLInputElement;
    if (input.value) {
      this.tagDate.set(new Date(input.value));
    }
  }
}
