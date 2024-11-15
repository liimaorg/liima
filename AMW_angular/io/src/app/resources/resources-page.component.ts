import { ChangeDetectionStrategy, Component, computed, inject, Signal, signal, OnDestroy } from '@angular/core';
import { AuthService } from '../auth/auth.service';
import { PageComponent } from '../layout/page/page.component';
import { LoadingIndicatorComponent } from '../shared/elements/loading-indicator.component';
import { ResourceTypesService } from './resource-types.service';
import { ResourceType } from '../resource/resource-type';
import { ButtonComponent } from '../shared/button/button.component';
import { IconComponent } from '../shared/icon/icon.component';
import { takeUntil } from 'rxjs/operators';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { ResourceTypeAddComponent } from './resource-type-add.component';
import { BehaviorSubject, Subject } from 'rxjs';
import { ToastService } from '../shared/elements/toast/toast.service';
import { ResourceTypeRequest } from '../resource/resource-type-request';

@Component({
  selector: 'app-resources-page',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [PageComponent, LoadingIndicatorComponent, ButtonComponent, IconComponent],
  templateUrl: './resources-page.component.html',
})
export class ResourcesPageComponent implements OnDestroy {
  private authService = inject(AuthService);
  private resourceTypesService = inject(ResourceTypesService);
  private modalService = inject(NgbModal);
  private toastService = inject(ToastService);

  predefinedResourceTypes: Signal<ResourceType[]> = this.resourceTypesService.predefinedResourceTypes;
  rootResourceTypes: Signal<ResourceType[]> = this.resourceTypesService.rootResourceTypes;
  isLoading = signal(false);
  expandedResourceTypeId: number | null = null;

  private error$ = new BehaviorSubject<string>('');
  private destroy$ = new Subject<void>();

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

  toggleChildrenResourceTypes(resourceType: ResourceType): void {
    this.expandedResourceTypeId = this.expandedResourceTypeId === resourceType.id ? null : resourceType.id;
  }

  addResourceType() {
    const modalRef = this.modalService.open(ResourceTypeAddComponent, {
      size: 'sm',
    });

    modalRef.componentInstance.resourceType = {
      newResourceTypeName: '',
      parentId: null,
    };

    modalRef.componentInstance.saveResourceType
      .pipe(takeUntil(this.destroy$))
      .subscribe((resourceTypeData: ResourceTypeRequest) => this.save(resourceTypeData));
  }

  save(resourceTypeData: ResourceTypeRequest): void {
    this.resourceTypesService
      .addNewResourceType(resourceTypeData)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => this.toastService.success('Resource type saved successfully.'),
        error: (err) => this.error$.next(err.message),
        complete: () => this.resourceTypesService.refreshData(),
      });
  }
}
