import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Release } from './release';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { KeyValuePipe, NgFor, NgForOf, NgIf } from '@angular/common';
import { ResourceEntity } from './resourceEntity';

@Component({
  selector: 'amw-release-delete',
  standalone: true,
  imports: [KeyValuePipe, NgIf, NgFor, NgForOf, FormsModule],
  templateUrl: './release-delete.component.html',
})
export class ReleaseDeleteComponent implements OnInit {
  @Input() release: Release;
  @Input() resources: Map<string, ResourceEntity[]>;
  @Output() deleteRelease: EventEmitter<Release> = new EventEmitter<Release>();

  hasResources: boolean = false;

  constructor(public activeModal: NgbActiveModal) {
    this.activeModal = activeModal;
  }

  ngOnInit(): void {
    if (this.resources) {
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
