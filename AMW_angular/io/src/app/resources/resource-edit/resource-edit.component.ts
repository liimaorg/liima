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
import { ResourceTagsComponent } from './resource-tags/resource-tags.component';
import { RESOURCE_TYPE } from '../../core/amw-constants';
import { CopyFromResourceDialogComponent } from './copy-from-resource-dialog/copy-from-resource-dialog.component';
import { ResourceApplicationsComponent } from './resource-applications/resource-applications.component';
import { ResourceRelationsComponent } from './resource-relations/resource-relations.component';

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
    ResourceRelationsComponent,
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

  id = toSignal(this.route.queryParamMap.pipe(map((params) => Number(params.get('id')))), { initialValue: 0 });
  contextId = toSignal(this.route.queryParamMap.pipe(map((params) => Number(params.get('ctx')))), { initialValue: 1 });
  relationId = toSignal(
    this.route.queryParamMap.pipe(map((params) => (params.get('rel') ? Number(params.get('rel')) : null))),
    { initialValue: null },
  );
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
        canEditResourceType: this.authService.hasPermission('RESOURCETYPE', 'READ'),
      };
    } else {
      return {
        canEditResource: false,
        canTestGenerate: false,
        canTagCurrentState: false,
        canCopyFromResource: false,
        canEditResourceType: false,
      };
    }
  });

  testGenerationQueryParams = computed(() => ({
    id: this.id(),
    ctx: this.contextId(),
    rel: this.relationId(),
  }));

  onRelationSelected(relationId: number | null) {
    this.router.navigate([], {
      queryParams: { rel: relationId },
      queryParamsHandling: 'merge',
    });
  }

  protected readonly showAnalyze = computed<boolean>(
    () => this.testGenerationAvailable() && this.permissions().canTestGenerate,
  );

  protected readonly showMore = computed<boolean>(() => this.permissions().canCopyFromResource);

  protected readonly goToResourceTypeAvailable = computed<boolean>(
    () => !!this.resource()?.resourceTypeId && this.permissions().canEditResourceType,
  );

  protected readonly goToApplicationServerAvailable = computed<boolean>(() => !!this.resource()?.applicationServerId);

  protected readonly goToDeploymentsAvailable = computed<boolean>(
    () => this.isApplicationServer() || !!this.resource()?.hasApplicationServer,
  );

  protected readonly goToAuditViewAvailable = computed<boolean>(() => !!this.id());

  protected readonly showGoTo = computed<boolean>(
    () =>
      this.goToResourceTypeAvailable() ||
      this.goToApplicationServerAvailable() ||
      this.goToDeploymentsAvailable() ||
      this.goToAuditViewAvailable(),
  );

  protected readonly resourceTypeQueryParams = computed(() => ({
    id: this.resource()?.resourceTypeId,
    ctx: this.contextId(),
  }));

  protected readonly applicationServerQueryParams = computed(() => ({
    id: this.resource()?.applicationServerId,
    ctx: this.contextId(),
  }));

  protected readonly deploymentsQueryParams = computed(() => {
    const res = this.resource();
    const filters: { name: string; val: string }[] = [];
    if (this.isApplicationServer()) {
      filters.push({ name: 'Application server', val: res?.name ?? '' });
    } else {
      if (res?.name) {
        filters.push({ name: 'Application', val: res.name });
      }
      if (res?.applicationServerName) {
        filters.push({ name: 'Application server', val: res.applicationServerName });
      }
    }
    return { filters: JSON.stringify(filters) };
  });

  protected readonly auditViewQueryParams = computed(() => ({ resourceId: this.id() }));

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
