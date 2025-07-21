import { ChangeDetectionStrategy, Component, EventEmitter, inject, Input, Output } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { FormsModule } from '@angular/forms';
import { ModalHeaderComponent } from '../../shared/modal-header/modal-header.component';
import { ButtonComponent } from '../../shared/button/button.component';
import { ResourceType } from '../../resource/resource-type';
import { Release } from '../../settings/releases/release';
import { NgSelectModule } from '@ng-select/ng-select';

@Component({
  selector: 'app-resource-add',
  imports: [FormsModule, ModalHeaderComponent, ButtonComponent, NgSelectModule],
  templateUrl: './resource-add.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ResourceAddComponent {
  activeModal = inject(NgbActiveModal);
  @Input() resourceType: ResourceType;
  @Input() releases: Release[];
  @Input() selectedReleaseName: Release;
  resourceName: string;
  @Output() saveResource: EventEmitter<any> = new EventEmitter<any>();

  getTitle() {
    if (!this.resourceType) return;
    return this.resourceType.name ? `Create new instance for ${this.resourceType.name}` : `Create new instance`;
  }

  save() {
    if (this.isValidForm()) {
      const resourceToAdd = {
        name: this.resourceName,
        type: this.resourceType.name,
        releaseName: this.selectedReleaseName,
      };
      this.saveResource.emit(resourceToAdd);
      this.activeModal.close();
    }
  }

  cancel() {
    this.activeModal.close();
  }

  setSelectedRelease($event: any) {
    this.selectedReleaseName = $event;
  }

  isValidForm() {
    const REGEXP_ALPHANUMERIC_WITH_UNDERSCORE_HYPHEN = /^[a-zA-Z0-9_-]+$/;
    return this.resourceName ? REGEXP_ALPHANUMERIC_WITH_UNDERSCORE_HYPHEN.test(this.resourceName) : false;
  }
}
