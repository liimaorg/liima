import { Component, EventEmitter, inject, Input, Output, Signal } from '@angular/core';
import { ResourceType } from '../models/resource-type';
import { ResourceTypesService } from '../services/resource-types.service';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { ModalHeaderComponent } from '../../shared/modal-header/modal-header.component';
import { ButtonComponent } from '../../shared/button/button.component';
import { FormsModule } from '@angular/forms';
import { ResourceTypeRequest } from '../models/resource-type-request';
import { NgSelectModule } from '@ng-select/ng-select';

@Component({
  selector: 'app-resource-type-add',
  imports: [ModalHeaderComponent, ButtonComponent, FormsModule, NgSelectModule],
  templateUrl: './resource-type-add.component.html',
})
export class ResourceTypeAddComponent {
  @Input() resourceType: ResourceType;
  @Output() saveResourceType: EventEmitter<ResourceTypeRequest> = new EventEmitter<ResourceTypeRequest>();
  private resourceTypesService = inject(ResourceTypesService);
  rootResourceTypes: Signal<ResourceType[]> = this.resourceTypesService.rootResourceTypes;
  parentId: number;

  constructor(public activeModal: NgbActiveModal) {}

  cancel() {
    this.activeModal.close();
  }

  save() {
    if (this.isValid()) {
      const request: ResourceTypeRequest = {
        name: this.resourceType.name,
        parentId: this.parentId,
      };
      this.saveResourceType.emit(request);
      this.activeModal.close();
    }
  }

  isValid(): boolean {
    const REGEXP_ALPHANUMERIC_WITH_UNDERSCORE_HYPHEN = /^[a-zA-Z0-9_-]+$/;
    return this.resourceType.name ? REGEXP_ALPHANUMERIC_WITH_UNDERSCORE_HYPHEN.test(this.resourceType.name) : false;
  }
}
