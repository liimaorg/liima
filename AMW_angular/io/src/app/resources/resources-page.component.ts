import {
  ChangeDetectionStrategy,
  Component,
  computed,
  inject,
  Signal,
  signal,
  OnDestroy,
  WritableSignal,
} from '@angular/core';
import { AuthService } from '../auth/auth.service';
import { PageComponent } from '../layout/page/page.component';
import { LoadingIndicatorComponent } from '../shared/elements/loading-indicator.component';
import { ResourceTypesService } from '../resource/resource-types.service';
import { ResourceType } from '../resource/resource-type';
import { ButtonComponent } from '../shared/button/button.component';
import { IconComponent } from '../shared/icon/icon.component';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { ResourceTypeAddComponent } from './resource-type-add/resource-type-add.component';
import { ResourceTypeRequest } from '../resource/resource-type-request';
import { ResourcesListComponent } from './resources-list/resources-list.component';
import { ResourceService } from '../resource/resource.service';
import { Resource } from '../resource/resource';
import { ReleasesService } from '../settings/releases/releases.service';
import { Release } from '../settings/releases/release';
import { ToastService } from '../shared/elements/toast/toast.service';
import { BehaviorSubject, Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';

@Component({
  selector: 'app-resources-page',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [PageComponent, LoadingIndicatorComponent, ButtonComponent, IconComponent, ResourcesListComponent],
  templateUrl: './resources-page.component.html',
})
export class ResourcesPageComponent implements OnDestroy {
  private authService = inject(AuthService);
  private resourceTypesService = inject(ResourceTypesService);
  private resourceService = inject(ResourceService);
  private releaseService = inject(ReleasesService);
  private toastService = inject(ToastService);
  private error$ = new BehaviorSubject<string>('');
  private destroy$ = new Subject<void>();
  private modalService = inject(NgbModal);

  predefinedResourceTypes: Signal<ResourceType[]> = this.resourceTypesService.predefinedResourceTypes;
  rootResourceTypes: Signal<ResourceType[]> = this.resourceTypesService.rootResourceTypes;
  resourceGroupListForType: Signal<Resource[]> = this.resourceService.resourceGroupListForType;
  releases: Signal<Release[]> = this.releaseService.allReleases;
  isLoading = signal(false);
  expandedResourceTypeId: number | null = null;
  selectedResourceType: WritableSignal<ResourceType | null> = signal(null);

  permissions = computed(() => {
    if (this.authService.restrictions().length > 0) {
      return {
        canViewResourceTypes: this.authService.hasPermission('RES_TYPE_LIST_TAB', 'ALL'),
        canCreateResourceTypes: this.authService.hasPermission('RESOURCETYPE', 'CREATE'),
      };
    } else {
      return { canViewResourceTypes: false, canCreateResourceTypes: false };
    }
  });

  ngOnDestroy(): void {
    this.destroy$.next(undefined);
  }
  selectedResourceTypeOrDefault: Signal<ResourceType> = computed(() => {
    if (!this.selectedResourceType() && this.rootResourceTypes() && this.rootResourceTypes().length > 0) {
      this.resourceService.setTypeForResourceGroupList(this.rootResourceTypes()[0]);
      return this.rootResourceTypes()[0];
    }
    return this.selectedResourceType() || null;
  });

  toggleChildrenAndOrLoadResourcesList(resourceType: ResourceType): void {
    this.resourceService.setTypeForResourceGroupList(resourceType);
    if (resourceType && resourceType.hasChildren)
      this.expandedResourceTypeId = this.expandedResourceTypeId === resourceType.id ? null : resourceType.id;
    this.selectedResourceType.set(resourceType);
  }

  addResource(resource: any) {
    this.resourceService
      .createResourceForResourceType(resource)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => this.toastService.success('Resource saved successfully.'),
        error: (e) => this.error$.next(e),
        complete: () => this.resourceService.setTypeForResourceGroupList(this.selectedResourceTypeOrDefault()), // refresh data of the selected resource type
      });
  }

  addResourceType() {
    const modalRef = this.modalService.open(ResourceTypeAddComponent, {
      size: 'sm',
    });

    modalRef.componentInstance.resourceType = {
      name: '',
      parentId: null,
    };

    modalRef.componentInstance.saveResourceType
      .pipe(takeUntil(this.destroy$))
      .subscribe((resourceTypeData: ResourceTypeRequest) => this.saveResourceType(resourceTypeData));
  }

  saveResourceType(resourceTypeData: ResourceTypeRequest): void {
    this.resourceTypesService
      .addNewResourceType(resourceTypeData)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => this.toastService.success('Resource type saved successfully.'),
        error: (err) => this.error$.next(err.message),
        complete: () => this.resourceTypesService.refreshData(),
      });
  }

  deleteResourceType(resourceType: ResourceType) {
    this.resourceTypesService
      .delete(resourceType.id)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => this.toastService.success('Resource type deleted successfully.'),
        error: (e) => this.error$.next(e),
        complete: () => {
          this.resourceTypesService.refreshData();
          this.selectedResourceType.set(null);
        },
      });
  }
}
