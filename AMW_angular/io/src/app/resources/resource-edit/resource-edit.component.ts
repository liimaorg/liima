import { Component, computed, inject, Signal } from '@angular/core';
import { LoadingIndicatorComponent } from '../../shared/elements/loading-indicator.component';
import { PageComponent } from '../../layout/page/page.component';
import { ActivatedRoute, RouterLink } from '@angular/router';
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
import { ResourceTagsComponent } from './resource-tags/resource-tags.component';
import { RESOURCE_TYPE } from '../../core/amw-constants';
import { CopyFromResourceDialogComponent } from './copy-from-resource-dialog/copy-from-resource-dialog.component';

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
    ResourceTagsComponent,
    RouterLink,
  ],
  templateUrl: './resource-edit.component.html',
  styleUrl: './resource-edit.component.scss',
})
export class ResourceEditComponent {
  private authService = inject(AuthService);
  private resourceService = inject(ResourceService);
  private route = inject(ActivatedRoute);
  private modalService = inject(NgbModal);

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

  protected readonly isApplicationServer = computed<boolean>(
    () => this.resource()?.type === RESOURCE_TYPE.APPLICATION_SERVER,
  );

  testGenerationAvailable = computed(() => {
    return this.isApplicationServer() || this.resource()?.hasApplicationServer;
  });

  permissions = computed(() => {
    if (this.authService.restrictions().length > 0) {
      const resourceTypeName = this.resource()?.type ?? null;
      const resourceGroupId = this.resource()?.resourceGroupId ?? null;
      return {
        canEditResource: this.authService.hasPermission('RESOURCE', 'READ'),
        canTestGenerate: this.authService.hasPermission('RESOURCE_TEST_GENERATION', 'READ'),
        canTagCurrentState: this.authService.hasPermission('RESOURCE', 'UPDATE', resourceTypeName, resourceGroupId),
        canCopyFromResource: this.authService.hasPermission(
          'RESOURCE_RELEASE_COPY_FROM_RESOURCE',
          'ALL',
          resourceTypeName,
          resourceGroupId,
        ),
      };
    } else {
      return { canEditResource: false, canTestGenerate: false, canTagCurrentState: false, canCopyFromResource: false };
    }
  });

  testGenerationQueryParams = computed(() => ({
    id: this.id(),
    ctx: this.contextId(),
  }));
  protected readonly showAnalyze = computed<boolean>(
    () => this.testGenerationAvailable() && this.permissions().canTestGenerate,
  );

  protected readonly showMore = computed<boolean>(() => this.permissions().canCopyFromResource);

  openCopyFromResourceDialog() {
    const modalRef = this.modalService.open(CopyFromResourceDialogComponent, { size: 'lg' });
    modalRef.componentInstance.resourceId = this.id();
    modalRef.componentInstance.resourceTypeName = this.resource()?.type;
    modalRef.result.then(
      (result) => {
        if (result === 'copied') {
          this.resourceService.setIdForResource(this.id());
        }
      },
      () => {},
    );
  }
}
