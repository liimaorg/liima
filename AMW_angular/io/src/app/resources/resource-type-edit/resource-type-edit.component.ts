import { Component, computed, inject, Signal } from '@angular/core';
import { LoadingIndicatorComponent } from '../../shared/elements/loading-indicator.component';
import { PageComponent } from '../../layout/page/page.component';
import { ActivatedRoute } from '@angular/router';
import { map } from 'rxjs/operators';
import { toSignal } from '@angular/core/rxjs-interop';
import { AuthService } from '../../auth/auth.service';
import { ResourceType } from '../models/resource-type';
import { ResourceTypesService } from '../services/resource-types.service';
import { ResourceTypeFunctionsListComponent } from './resource-type-functions/resource-type-functions-list.component';
import { ResourceTypeTemplatesListComponent } from './resource-type-templates/resource-type-templates-list.component';
import { ContextsListComponent } from '../contexts-list/contexts-list.component';

@Component({
  selector: 'app-resource-type-edit',
  standalone: true,
  imports: [
    LoadingIndicatorComponent,
    PageComponent,
    ResourceTypeFunctionsListComponent,
    ResourceTypeTemplatesListComponent,
    ContextsListComponent,
  ],
  templateUrl: './resource-type-edit.component.html',
})
export class ResourceTypeEditComponent {
  private authService = inject(AuthService);
  private resourceTypeService = inject(ResourceTypesService);
  private route = inject(ActivatedRoute);

  id = toSignal(this.route.queryParamMap.pipe(map((params) => Number(params.get('id')))), { initialValue: 0 });
  contextId = toSignal(this.route.queryParamMap.pipe(map((params) => Number(params.get('ctx')))), { initialValue: 1 });
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
}
