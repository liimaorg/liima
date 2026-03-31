import { Component, computed, inject, Signal } from '@angular/core';
import { LoadingIndicatorComponent } from '../../shared/elements/loading-indicator.component';
import { PageComponent } from '../../layout/page/page.component';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { map } from 'rxjs/operators';
import { toSignal } from '@angular/core/rxjs-interop';
import { ResourceService } from '../services/resource.service';
import { Resource } from '../models/resource';
import { AuthService } from '../../auth/auth.service';
import { ResourceFunctionsListComponent } from './resource-functions/resource-functions-list/resource-functions-list.component';
import { ResourceTemplatesListComponent } from './resource-templates/resource-templates-list/resource-templates-list.component';
import { Release } from '../models/release';
import { ButtonComponent } from '../../shared/button/button.component';
import { NgbDropdown, NgbDropdownItem, NgbDropdownMenu, NgbDropdownToggle, NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { ContextsListComponent } from '../contexts-list/contexts-list.component';
import { ResourcePropertiesComponent } from './resource-properties/resource-properties.component';
import { ResourceReleasesComponent } from './resource-releases/resource-releases.component';
import { TagEditModalComponent, TagData } from './tag-edit-modal/tag-edit-modal.component';
import { ResourceTagsService } from '../services/resource-tags.service';
import { ToastService } from '../../shared/elements/toast/toast.service';
import { ResourceApplicationsComponent } from './resource-applications/resource-applications.component';

@Component({
  selector: 'app-resource-edit',
  standalone: true,
  imports: [
    LoadingIndicatorComponent,
    PageComponent,
    ResourceFunctionsListComponent,
    ResourceTemplatesListComponent,
    ButtonComponent,
    NgbDropdown,
    NgbDropdownMenu,
    NgbDropdownToggle,
    NgbDropdownItem,
    ContextsListComponent,
    ResourcePropertiesComponent,
    ResourceReleasesComponent,
    RouterLink,
    ResourceApplicationsComponent,
  ],
  templateUrl: './resource-edit.component.html',
  styleUrl: './resource-edit.component.scss',
})
export class ResourceEditComponent {
  private authService = inject(AuthService);
  private resourceService = inject(ResourceService);
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private modalService = inject(NgbModal);
  private resourceTagsService = inject(ResourceTagsService);
  private toastService = inject(ToastService);

  id = toSignal(this.route.queryParamMap.pipe(map((params) => Number(params.get('id')))), { initialValue: 0 });
  contextId = toSignal(this.route.queryParamMap.pipe(map((params) => Number(params.get('ctx')))), { initialValue: 1 });
  resource: Signal<Resource> = this.resourceService.resource;
  releases: Signal<Release[]> = this.resourceService.releasesForResourceGroup;

  isLoading = computed(() => {
    if (this.id()) {
      this.resourceService.setIdForResource(this.id());
      return false;
    } else return false;
  });

  testGenerationAvailable = computed(() => {
    return this.resource()?.type === 'APPLICATIONSERVER' || this.resource()?.hasApplicationServer;
  });

  permissions = computed(() => {
    if (this.authService.restrictions().length > 0) {
      const resourceTypeName = this.resource()?.type ?? null;
      const resourceGroupId = this.resource()?.resourceGroupId ?? null;
      return {
        canEditResource: this.authService.hasPermission('RESOURCE', 'READ'),
        canTestGenerate: this.authService.hasPermission('RESOURCE_TEST_GENERATION', 'READ'),
        canTagCurrentState: this.authService.hasPermission('RESOURCE', 'UPDATE', resourceTypeName, resourceGroupId),
      };
    } else {
      return { canEditResource: false, canTestGenerate: false, canTagCurrentState: false };
    }
  });

  selectedRelease = computed(() => {
    return this.releases().find((release) => release.id === this.id());
  });

  testGenerationQueryParams = computed(() => ({
    id: this.id(),
    ctx: this.contextId(),
  }));
  protected readonly showAnalyze = computed<boolean>(
    () => this.testGenerationAvailable() && this.permissions().canTestGenerate,
  );

  protected readonly isApplicationServer = computed<boolean>(
    () => this.resource()?.type === 'APPLICATIONSERVER',
  );

  protected readonly showMoreMenu = computed<boolean>(
    () => this.permissions().canTagCurrentState && this.isApplicationServer(),
  );

  loadResourceFromRelease(releaseId: number) {
    void this.router.navigate([], {
      relativeTo: this.route,
      queryParams: { id: releaseId },
      queryParamsHandling: 'merge',
    });
  }

  openTagDialog() {
    const modalRef = this.modalService.open(TagEditModalComponent);
    modalRef.componentInstance.resource = this.resource();
    modalRef.componentInstance.saveTag.subscribe((tagData: TagData) => this.createTag(tagData));
  }

  private createTag(tagData: TagData) {
    this.resourceTagsService
      .createTag(this.resource().id, {
        label: tagData.label,
        tagDate: tagData.tagDate,
      })
      .subscribe({
        next: () => {
          this.toastService.success(`New tag '${tagData.label}' created.`);
        },
        error: (error) => {
          console.error('Failed to create tag:', error);
          const errorMessage = error?.error?.message || 'Failed to create tag.';
          this.toastService.error(errorMessage);
        },
      });
  }
}
