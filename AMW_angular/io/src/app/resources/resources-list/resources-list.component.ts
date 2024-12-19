import { Component, computed, inject, input, output } from '@angular/core';
import { Resource } from '../../resource/resource';
import { ResourceType } from '../../resource/resource-type';
import { ButtonComponent } from '../../shared/button/button.component';
import { IconComponent } from '../../shared/icon/icon.component';
import { NgbModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { takeUntil } from 'rxjs/operators';
import { Subject } from 'rxjs';
import { ResourceAddComponent } from '../resource-add/resource-add.component';
import { Release } from '../../settings/releases/release';
import { AuthService } from '../../auth/auth.service';
import { ResourceTypeDeleteComponent } from '../resource-type-delete/resource-type-delete.component';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-resources-list',
  standalone: true,
  templateUrl: './resources-list.component.html',
  styleUrl: './resources-list.component.scss',
  imports: [ButtonComponent, IconComponent, RouterLink],
})
export class ResourcesListComponent {
  private modalService = inject(NgbModal);
  private authService = inject(AuthService);
  private destroy$ = new Subject<void>();
  resourceType = input.required<ResourceType>();
  resourceGroupList = input.required<Resource[]>();
  releases = input.required<Release[]>();
  resourceToAdd = output<any>();
  resourceTypeToDelete = output<ResourceType>();

  permissions = computed(() => {
    if (this.authService.restrictions().length > 0) {
      return {
        canReadResources: this.authService.hasPermission('RESOURCE', 'READ'),
        canCreateResource: this.authService.hasPermission('RESOURCE', 'CREATE'),
        canDeleteResourceType: this.authService.hasPermission('RESOURCETYPE', 'DELETE'),
      };
    } else {
      return {
        canReadResources: false,
        canCreateResource: false,
        canDeleteResourceType: false,
      };
    }
  });

  addResource() {
    const modalRef: NgbModalRef = this.modalService.open(ResourceAddComponent);
    modalRef.componentInstance.resourceType = this.resourceType();
    modalRef.componentInstance.releases = this.releases();
    modalRef.componentInstance.selectedReleaseName = this.releases()[0].name;
    modalRef.componentInstance.saveResource
      .pipe(takeUntil(this.destroy$))
      .subscribe((resource: any) => this.resourceToAdd.emit(resource));
  }

  deleteResourceType() {
    const modalRef: NgbModalRef = this.modalService.open(ResourceTypeDeleteComponent);
    modalRef.componentInstance.resourceType = this.resourceType();
    modalRef.componentInstance.resourceTypeToDelete
      .pipe(takeUntil(this.destroy$))
      .subscribe((resourceType: ResourceType) => this.resourceTypeToDelete.emit(resourceType));
  }
}
