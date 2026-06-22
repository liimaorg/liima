import { ChangeDetectionStrategy, Component, computed, inject, Signal, signal, WritableSignal } from '@angular/core';
import { NgClass } from '@angular/common';
import { AuthService } from '../auth/auth.service';
import { PageComponent } from '../layout/page/page.component';
import { LoadingIndicatorComponent } from '../shared/elements/loading-indicator.component';
import { ResourceTypesService } from './services/resource-types.service';
import { ResourceType } from './models/resource-type';
import { ButtonComponent } from '../shared/button/button.component';
import { IconComponent } from '../shared/icon/icon.component';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { ResourceTypeAddComponent } from './resource-type-add/resource-type-add.component';
import { ResourceTypeRequest } from './models/resource-type-request';
import { ResourcesListComponent } from './resources-list/resources-list.component';
import { ResourceService } from './services/resource.service';
import { Resource } from './models/resource';
import { ReleasesService } from '../settings/releases/releases.service';
import { Release } from '../settings/releases/release';
import { ToastService } from '../shared/elements/toast/toast.service';
import { Subject } from 'rxjs';
import { map, takeUntil } from 'rxjs/operators';
import { ActivatedRoute, Router } from '@angular/router';
import { toSignal } from '@angular/core/rxjs-interop';

@Component({
  selector: 'app-resources-page',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [PageComponent, LoadingIndicatorComponent, ButtonComponent, IconComponent, ResourcesListComponent, NgClass],
  templateUrl: './resources-page.component.html',
  styleUrl: 'resources-page.component.scss',
})
export class ResourcesPageComponent {
  private authService = inject(AuthService);
  private resourceTypesService = inject(ResourceTypesService);
  private resourceService = inject(ResourceService);
  private releaseService = inject(ReleasesService);
  private toastService = inject(ToastService);
  private destroy$ = new Subject<void>();
  private modalService = inject(NgbModal);
  private route = inject(ActivatedRoute);
  private router = inject(Router);

  predefinedResourceTypes: Signal<ResourceType[]> = this.resourceTypesService.predefinedResourceTypes;
  rootResourceTypes: Signal<ResourceType[]> = this.resourceTypesService.rootResourceTypes;
  resourceGroupListForType: Signal<Resource[]> = this.resourceService.resourceGroupListForType;
  releases: Signal<Release[]> = this.releaseService.allReleases;
  isLoading = signal(false);
  expandedResourceTypeId: number | null = null;
  expandedItems: ResourceType[] = [];
  selectedResourceType: WritableSignal<ResourceType | null> = signal(null);
  selection: any;

  private selectedResourceTypeIdFromUrl = toSignal(
    this.route.queryParamMap.pipe(
      map((params) => {
        const id = params.get('selectedResourceTypeId');
        return id ? Number(id) : null;
      }),
    ),
    { initialValue: null },
  );

  private autoSelectTrigger = computed(() => {
    const idFromUrl = this.selectedResourceTypeIdFromUrl();
    if (idFromUrl === null) return;

    const allTypes = [...(this.predefinedResourceTypes() || []), ...(this.rootResourceTypes() || [])];
    if (allTypes.length === 0) return;

    const resourceType = this.findTypeById(allTypes, idFromUrl);
    if (resourceType) {
      // Expand parent chain so child is visible in tree, get the immediate parent
      const parentType = this.expandParents(allTypes, idFromUrl);
      // Safely execute selection out-of-stack to avoid expression-changed bugs
      setTimeout(() => this.selectFromUrl(resourceType, parentType));
    }
  });

  private findTypeById(types: ResourceType[], id: number): ResourceType | null {
    for (const type of types) {
      if (type.id === id) return type;
      if (type.children?.length) {
        const found = this.findTypeById(type.children, id);
        if (found) return found;
      }
    }
    return null;
  }

  private expandParents(types: ResourceType[], targetId: number): ResourceType | null {
    for (const type of types) {
      // Check if target is a direct child
      const isDirectChild = type.children?.some((c) => c.id === targetId);
      if (isDirectChild) {
        // Add this parent to expanded items
        if (!this.expandedItems.find((e) => e.id === type.id)) {
          this.expandedItems.push(type);
        }
        return type;
      }
      // Check if target is in deeper descendants
      if (type.children?.length) {
        const foundParent = this.expandParents(type.children, targetId);
        if (foundParent) {
          // Add this ancestor to expanded items
          if (!this.expandedItems.find((e) => e.id === type.id)) {
            this.expandedItems.push(type);
          }
          return foundParent; // Return the immediate parent (deepest level)
        }
      }
    }
    return null;
  }

  private selectFromUrl(resourceType: ResourceType, parentType: ResourceType | null): void {
    this.selection = resourceType;
    this.resourceService.setTypeForResourceGroupList(resourceType);
    // For parent with children: expand it and update expandedItems
    if (resourceType.hasChildren) {
      this.getUpdateExpandedItems(resourceType);
      this.expandedResourceTypeId = resourceType.id;
    } else {
      // For leaf: expand parent so child is visible
      this.expandedResourceTypeId = parentType?.id ?? null;
    }
    this.selectedResourceType.set(resourceType);
  }

  constructor() {
    // Simply reference the computed signal so it stays active and listens for changes
    this.autoSelectTrigger();
  }

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

  selectedResourceTypeOrDefault: Signal<ResourceType | null> = computed(() => {
    if (!this.selectedResourceType() && this.rootResourceTypes() && this.rootResourceTypes().length > 0) {
      this.resourceService.setTypeForResourceGroupList(this.rootResourceTypes()[0]);
      return this.rootResourceTypes()[0];
    }
    return this.selectedResourceType() || null;
  });

  toggleChildrenAndOrLoadResourcesList(resourceType: ResourceType, updateUrl: boolean = true): void {
    this.selection = resourceType;
    this.resourceService.setTypeForResourceGroupList(resourceType);
    if (resourceType && resourceType.hasChildren) this.getUpdateExpandedItems(resourceType);
    this.expandedResourceTypeId = this.expandedResourceTypeId === resourceType.id ? null : resourceType.id;
    this.selectedResourceType.set(resourceType);

    if (updateUrl) {
      this.router
        .navigate([], {
          relativeTo: this.route,
          queryParams: { selectedResourceTypeId: resourceType.id },
          queryParamsHandling: 'merge',
        })
        .catch((error) => {
          console.error('Navigation error:', error);
        });
    }
  }

  isExpanded(resourceType: ResourceType) {
    return this.expandedItems.find((element) => element.id === resourceType.id);
  }

  addResource(resource: any) {
    this.resourceService
      .createResourceForResourceType(resource)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => this.toastService.success('Resource saved successfully.'),
        error: (e) => console.error('Failed to create resource:', e),
        complete: () => {
          const rt = this.selectedResourceTypeOrDefault();
          if (rt) this.resourceService.setTypeForResourceGroupList(rt);
        },
      });
  }

  addResourceType() {
    const modalRef = this.modalService.open(ResourceTypeAddComponent, {
      size: 'md',
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
        error: (err) => console.error('Failed to save resource type:', err),
        complete: () => this.resourceTypesService.refreshData(),
      });
  }

  deleteResourceType(resourceType: ResourceType) {
    this.resourceTypesService
      .delete(resourceType.id!)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => this.toastService.success('Resource type deleted successfully.'),
        error: (e) => console.error('Failed to create resource:', e),
        complete: () => {
          this.resourceTypesService.refreshData();
          this.selectedResourceType.set(null);
        },
      });
  }

  ngOnDestroy(): void {
    this.destroy$.next(undefined);
  }

  private getUpdateExpandedItems(resourceType: ResourceType) {
    const index = this.expandedItems?.findIndex((element: ResourceType) => element.id === resourceType.id);
    if (index > -1) {
      this.expandedItems.splice(index, 1);
    } else {
      this.expandedItems.push(resourceType);
    }
  }
}
