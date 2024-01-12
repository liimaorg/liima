import { Component, EventEmitter, Input, Output } from '@angular/core';
import { NgFor, NgIf } from '@angular/common';
import { NgSelectModule } from '@ng-select/ng-select';
import { FormsModule } from '@angular/forms';
import { IconComponent } from '../../shared/icon/icon.component';
import { Release } from './release';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';

@Component({
  selector: 'amw-release-edit',
  templateUrl: './release-edit.component.html',
  styleUrl: './release-edit.component.scss',
  standalone: true,
  imports: [NgIf, NgSelectModule, FormsModule, NgFor, IconComponent],
})
export class ReleaseEditComponent {
  @Input()
  selectedRelease: Release;
  @Output() cancelEdit: EventEmitter<boolean> = new EventEmitter<boolean>();
  @Output() saveRelease: EventEmitter<Release> = new EventEmitter<Release>();

  constructor(public activeModal: NgbActiveModal) {
    this.activeModal = activeModal;
  }

  getTitle(): string {
    return 'Add';
  }

  cancel() {
    this.activeModal.close();
  }

  save() {
    const release: Release = {
      name: this.selectedRelease.name,
      mainRelease: this.selectedRelease.mainRelease,
      description: this.selectedRelease.description,
      installationInProductionAt: this.selectedRelease.installationInProductionAt,
      id: this.selectedRelease.id,
      default: false,
    };
    this.saveRelease.emit(release);
  }
}
