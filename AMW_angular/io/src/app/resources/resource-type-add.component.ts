import { Component, EventEmitter, inject, Input, Output } from '@angular/core';
import { ResourceType } from '../resource/resource-type';
import { ResourceTypesService } from './resource-types.service';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { ModalHeaderComponent } from '../shared/modal-header/modal-header.component';

@Component({
  selector: 'app-resource-type-add',
  standalone: true,
  imports: [ModalHeaderComponent],
  templateUrl: './resource-type-add.component.html',
})
export class ResourceTypeAddComponent {
  @Input() resourceType: ResourceType;
  @Output() saveResourceType: EventEmitter<ResourceType> = new EventEmitter<ResourceType>();

  private resourceTypesService = inject(ResourceTypesService);

  constructor(public activeModal: NgbActiveModal) {}

  cancel() {
    this.activeModal.close();
    this.resourceTypesService.refreshData();
  }

  save() {
    this.saveResourceType.emit(this.resourceType);
    this.activeModal.close();
  }
}
