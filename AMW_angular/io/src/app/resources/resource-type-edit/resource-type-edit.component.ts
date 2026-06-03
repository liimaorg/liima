import { Component, computed, inject, Signal } from '@angular/core';
import { LoadingIndicatorComponent } from '../../shared/elements/loading-indicator.component';
import { PageComponent } from '../../layout/page/page.component';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { map } from 'rxjs/operators';
import { toSignal } from '@angular/core/rxjs-interop';
import { AuthService } from '../../auth/auth.service';
import { ResourceType } from '../models/resource-type';
import { ResourceTypesService } from '../services/resource-types.service';
import { ResourceTypeFunctionsListComponent } from './resource-type-functions/resource-type-functions-list.component';
import { ResourceTypeTemplatesListComponent } from './resource-type-templates/resource-type-templates-list.component';
import { ContextsListComponent } from '../contexts-list/contexts-list.component';
import { ResourceTypePropertiesComponent } from './resource-type-properties/resource-type-properties.component';
import { ResourceTypeRelationsComponent } from './resource-type-relations/resource-type-relations.component';
import { ButtonComponent } from '../../shared/button/button.component';
import { NgbDropdown, NgbDropdownItem, NgbDropdownMenu, NgbDropdownToggle } from '@ng-bootstrap/ng-bootstrap';
import { RESOURCE_TYPE } from '../../core/amw-constants';

@Component({
  selector: 'app-resource-type-edit',
  standalone: true,
  imports: [
    LoadingIndicatorComponent,
    PageComponent,
    ResourceTypeFunctionsListComponent,
    ResourceTypeTemplatesListComponent,
    ContextsListComponent,
    ResourceTypePropertiesComponent,
    ResourceTypeRelationsComponent,
    ButtonComponent,
    NgbDropdown,
    NgbDropdownMenu,
    NgbDropdownToggle,
    NgbDropdownItem,
    RouterLink,
  ],
  templateUrl: './resource-type-edit.component.html',
  styleUrl: './resource-type-edit.component.scss',
})
export class ResourceTypeEditComponent {
  private authService = inject(AuthService);
  private resourceTypeService = inject(ResourceTypesService);
  private route = inject(ActivatedRoute);

  id = toSignal(this.route.queryParamMap.pipe(map((params) => Number(params.get('id')))), { initialValue: 0 });
  contextId = toSignal(this.route.queryParamMap.pipe(map((params) => Number(params.get('ctx')))), { initialValue: 1 });
  relationId = toSignal(this.route.queryParamMap.pipe(map((params) => Number(params.get('rel')))), {
    initialValue: null,
  });
  resourceType: Signal<ResourceType> = this.resourceTypeService.resourceType;

  isLoading = computed(() => {
    if (this.id()) {
      this.resourceTypeService.setIdForResourceType(this.id());
      return false;
    } else return false;
  });

  permissions = computed(() => {
    if (this.authService.restrictions().length > 0) {
      return {
        canEditResourceType: this.authService.hasPermission('RESOURCETYPE', 'READ'),
      };
    } else {
      return { canEditResourceType: false };
    }
  });

  protected readonly goToDeploymentsAvailable = computed<boolean>(
    () => this.resourceType()?.isApplication || this.resourceType()?.name === RESOURCE_TYPE.APPLICATION_SERVER,
  );

  protected readonly showGoTo = computed<boolean>(() => this.goToDeploymentsAvailable());

  protected readonly deploymentsQueryParams = computed(() => ({ filters: JSON.stringify([]) }));
}
