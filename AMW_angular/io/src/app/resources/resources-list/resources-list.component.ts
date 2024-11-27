import { Component, inject, input } from '@angular/core';
import { Resource } from '../../resource/resource';
import { ResourceType } from '../../resource/resource-type';
import { ButtonComponent } from '../../shared/button/button.component';
import { IconComponent } from '../../shared/icon/icon.component';
import { NgbModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { ResourceAddComponent } from '../resource-add/resource-add.component';

@Component({
  selector: 'app-resources-list',
  standalone: true,
  templateUrl: './resources-list.component.html',
  imports: [ButtonComponent, IconComponent],
})
export class ResourcesListComponent {
  private modalService = inject(NgbModal);
  resourceType = input.required<ResourceType>();
  resourceGroupList = input<Resource[]>();

  addResource() {
    const modalRef: NgbModalRef = this.modalService.open(ResourceAddComponent);
    modalRef.componentInstance.resourceType = this.resourceType();
  }
}
