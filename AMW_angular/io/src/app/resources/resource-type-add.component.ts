import { Component, EventEmitter, inject, Input, Output, Signal } from '@angular/core';
import { ResourceType } from '../resource/resource-type';
import { ResourceTypesService } from './resource-types.service';
import {
  NgbActiveModal,
  NgbDropdown,
  NgbDropdownItem,
  NgbDropdownMenu,
  NgbDropdownToggle,
} from '@ng-bootstrap/ng-bootstrap';
import { ModalHeaderComponent } from '../shared/modal-header/modal-header.component';
import { ButtonComponent } from '../shared/button/button.component';
import { FormsModule } from '@angular/forms';
import { ResourceTypeRequest } from '../resource/resource-type-request';

@Component({
  selector: 'app-resource-type-add',
  standalone: true,
  imports: [
    ModalHeaderComponent,
    ButtonComponent,
    FormsModule,
    NgbDropdown,
    NgbDropdownItem,
    NgbDropdownMenu,
    NgbDropdownToggle,
  ],
  templateUrl: './resource-type-add.component.html',
})
export class ResourceTypeAddComponent {
  @Input() resourceType: ResourceType;
  @Output() saveResourceType: EventEmitter<ResourceTypeRequest> = new EventEmitter<ResourceTypeRequest>();
  private resourceTypesService = inject(ResourceTypesService);
  resourceTypeName: string;
  rootResourceTypes: Signal<ResourceType[]> = this.resourceTypesService.rootResourceTypes;

  constructor(public activeModal: NgbActiveModal) {}

  cancel() {
    this.activeModal.close();
    this.resourceTypesService.refreshData();
  }

  save() {
    const request: ResourceTypeRequest = {
      newResourceTypeName: this.resourceType.name,
      parentId: undefined,
    };
    this.saveResourceType.emit(request);
    this.activeModal.close();
  }

  selectResourceType(displayName: string): void {
    this.resourceTypeName = displayName;
  }
}
