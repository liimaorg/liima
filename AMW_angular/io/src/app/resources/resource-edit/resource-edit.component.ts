import { Component, computed, inject, Signal } from '@angular/core';
import { LoadingIndicatorComponent } from '../../shared/elements/loading-indicator.component';
import { PageComponent } from '../../layout/page/page.component';
import { ActivatedRoute } from '@angular/router';
import { map } from 'rxjs/operators';
import { toSignal } from '@angular/core/rxjs-interop';
import { ResourceService } from '../../resource/resource.service';
import { Resource } from '../../resource/resource';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { TileComponent } from '../../shared/tile/tile.component';
import { AuthService } from '../../auth/auth.service';
import { ResourceFunctionsListComponent } from './resource-functions/resource-functions-list.component';
import { ResourceTemplatesListComponent } from './resource-templates/resource-templates-list.component';

@Component({
  selector: 'app-resource-edit',
  standalone: true,
  imports: [
    LoadingIndicatorComponent,
    PageComponent,
    TileComponent,
    ResourceFunctionsListComponent,
    ResourceTemplatesListComponent,
  ],
  templateUrl: './resource-edit.component.html',
})
export class ResourceEditComponent {
  private authService = inject(AuthService);
  private modalService = inject(NgbModal);
  private resourceService = inject(ResourceService);
  private route = inject(ActivatedRoute);

  id = toSignal(this.route.queryParamMap.pipe(map((params) => Number(params.get('id')))), { initialValue: 0 });
  contextId = toSignal(this.route.queryParamMap.pipe(map((params) => Number(params.get('ctx')))), { initialValue: 1 });
  resource: Signal<Resource> = this.resourceService.resource;

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

  add() {
    this.modalService.open('This would open a modal to add something');
  }
  private editFunction(id: number) {
    this.modalService.open('This would open a modal to edit function with id:' + id);
  }

  private deleteFunction(id: number) {
    this.modalService.open('This would open a modal to delete function with id:' + id);
  }

  private overwriteFunction(id: number) {
    this.modalService.open('This would open a modal to overwrite function with id:' + id);
  }
}
