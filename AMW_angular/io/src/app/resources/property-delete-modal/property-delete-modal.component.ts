import { Component, input, output } from '@angular/core';
import { NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { PropertyDeleteModalService } from '../services/property-delete-modal.service';

@Component({
  selector: 'app-property-delete-modal',
  standalone: true,
  imports: [],
  templateUrl: './property-delete-modal.component.html',
  styleUrl: './property-delete-modal.component.scss',
})
export class PropertyDeleteModalComponent {
  deleteModalService = input.required<PropertyDeleteModalService>();
  modal = input.required<NgbModalRef>();
  confirmDelete = output<NgbModalRef>();

  onConfirmDelete() {
    this.confirmDelete.emit(this.modal());
  }

  onDismiss() {
    this.modal().dismiss();
  }
}
