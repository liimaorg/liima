import { Component, computed, inject, Signal } from '@angular/core';
import { LoadingIndicatorComponent } from '../../shared/elements/loading-indicator.component';
import { PageComponent } from '../../layout/page/page.component';
import { ActivatedRoute } from '@angular/router';
import { map } from 'rxjs/operators';
import { toSignal } from '@angular/core/rxjs-interop';
import { ResourceService } from '../../resource/resource.service';
import { Resource } from '../../resource/resource';
import { AuthService } from '../../auth/auth.service';
import { ResourceFunctionsListComponent } from './resource-functions/resource-functions-list.component';
import { ResourceTemplatesListComponent } from './resource-templates/resource-templates-list.component';
import { Release } from '../../resource/release';
import { ButtonComponent } from '../../shared/button/button.component';
import { NgbDropdown, NgbDropdownItem, NgbDropdownMenu, NgbDropdownToggle } from '@ng-bootstrap/ng-bootstrap';

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
  ],
  templateUrl: './resource-edit.component.html',
  styleUrl: './resource-edit.component.scss',
})
export class ResourceEditComponent {
  private authService = inject(AuthService);
  private resourceService = inject(ResourceService);
  private route = inject(ActivatedRoute);

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

  permissions = computed(() => {
    if (this.authService.restrictions().length > 0) {
      return {
        canEditResource: this.authService.hasPermission('RESOURCE', 'READ'),
      };
    } else {
      return { canEditResource: false };
    }
  });

  dummyClick() {
    //do nothing
  }
}
