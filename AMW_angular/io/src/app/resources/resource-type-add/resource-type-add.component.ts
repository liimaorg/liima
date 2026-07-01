import { Component, EventEmitter, inject, Input, Output, Signal, signal } from '@angular/core';
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
  activeModal = inject(NgbActiveModal);

  private readonly resourceTypeSignal = signal<ResourceType | null>(null);

  // ng-bootstrap modal inputs are assigned through componentInstance; keep setter-backed signals until
  // https://github.com/ng-bootstrap/ng-bootstrap/issues/4664 is resolved.
  @Input({ required: true })
  set resourceType(value: ResourceType) {
    this.resourceTypeSignal.set(value);
  }

  get resourceType(): ResourceType {
    return this.resourceTypeSignal() as ResourceType;
  }
  @Output() saveResourceType: EventEmitter<ResourceTypeRequest> = new EventEmitter<ResourceTypeRequest>();
  private resourceTypesService = inject(ResourceTypesService);
  rootResourceTypes: Signal<ResourceType[]> = this.resourceTypesService.rootResourceTypes;
  parentId!: number;

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
