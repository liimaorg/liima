import { Component, EventEmitter, Input, OnInit, Output, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Release } from './release';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { KeyValuePipe } from '@angular/common';
import { ResourceEntity } from './resource-entity';
import { ModalHeaderComponent } from '../../shared/modal-header/modal-header.component';
import { ButtonComponent } from '../../shared/button/button.component';

@Component({
  selector: 'app-release-delete',
  imports: [KeyValuePipe, FormsModule, ModalHeaderComponent, ButtonComponent],
  templateUrl: './release-delete.component.html',
})
export class ReleaseDeleteComponent implements OnInit {
  activeModal = inject(NgbActiveModal);

  @Input() release: Release;
  @Input() resources: Map<string, ResourceEntity[]>;
  @Output() deleteRelease: EventEmitter<Release> = new EventEmitter<Release>();

  hasResources: boolean = false;

  ngOnInit(): void {
    if (this.resources.size > 0) {
      this.hasResources = true;
    }
  }

  getTitle(): string {
    return 'Remove release';
  }

  cancel() {
    this.activeModal.close();
  }

  delete() {
    this.deleteRelease.emit(this.release);
    this.activeModal.close();
  }
}
