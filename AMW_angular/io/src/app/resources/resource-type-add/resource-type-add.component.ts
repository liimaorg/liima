import { Component, EventEmitter, inject, Input, Output, Signal } from '@angular/core';
import { ResourceType } from '../../resource/resource-type';
import { ResourceTypesService } from '../../resource/resource-types.service';
import {
  NgbActiveModal,
  NgbDropdown,
  NgbDropdownItem,
  NgbDropdownMenu,
  NgbDropdownToggle,
} from '@ng-bootstrap/ng-bootstrap';
import { ModalHeaderComponent } from '../../shared/modal-header/modal-header.component';
import { ButtonComponent } from '../../shared/button/button.component';
import { FormsModule } from '@angular/forms';
import { ResourceTypeRequest } from '../../resource/resource-type-request';

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
  styleUrl: './resource-type-add.component.scss',
})
export class ResourceTypeAddComponent {
  @Input() resourceType: ResourceType;
  @Output() saveResourceType: EventEmitter<ResourceTypeRequest> = new EventEmitter<ResourceTypeRequest>();
  private resourceTypesService = inject(ResourceTypesService);
  parentResourceTypeName: string;
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

  selectResourceType(displayName: string, parentId: number): void {
    this.parentResourceTypeName = displayName;
    this.parentId = parentId;
  }

  isValid(): boolean {
    const REGEXP_ALPHANUMERIC_WITH_UNDERSCORE_HYPHEN = /^[a-zA-Z0-9_-]+$/;
    return this.resourceType.name ? REGEXP_ALPHANUMERIC_WITH_UNDERSCORE_HYPHEN.test(this.resourceType.name) : false;
  }
}
