import { Component, EventEmitter, Input, OnInit, Output, inject, signal } from '@angular/core';
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

  private readonly releaseSignal = signal<Release | null>(null);
  private readonly resourcesSignal = signal<Map<string, ResourceEntity[]> | null>(null);

  // ng-bootstrap modal inputs are assigned through componentInstance; keep setter-backed signals until
  // https://github.com/ng-bootstrap/ng-bootstrap/issues/4664 is resolved.
  @Input({ required: true })
  set release(value: Release) {
    this.releaseSignal.set(value);
  }

  get release(): Release {
    return this.releaseSignal() as Release;
  }

  @Input({ required: true })
  set resources(value: Map<string, ResourceEntity[]>) {
    this.resourcesSignal.set(value);
  }

  get resources(): Map<string, ResourceEntity[]> {
    return this.resourcesSignal() as Map<string, ResourceEntity[]>;
  }
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
