import { Component, computed, inject, Signal, signal } from '@angular/core';
import { LoadingIndicatorComponent } from '../../shared/elements/loading-indicator.component';
import { PageComponent } from '../../layout/page/page.component';
import { ActivatedRoute } from '@angular/router';
import { map } from 'rxjs/operators';
import { toSignal } from '@angular/core/rxjs-interop';
import { ResourceService } from '../../resource/resource.service';
import { Resource } from '../../resource/resource';
import { EntryAction, TileListEntry, TileListEntryOutput } from '../../shared/tile/tile-list/tile-list.component';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { TileComponent } from '../../shared/tile/tile.component';
import { ResourceEditFunctionsComponent } from './resource-edit-functions/resource-edit-functions.component';
import { AuthService } from '../../auth/auth.service';
import { ResourceType } from '../../resource/resource-type';
import { ResourceTypesService } from '../../resource/resource-types.service';

@Component({
  selector: 'app-resources-edit-page',
  standalone: true,
  imports: [LoadingIndicatorComponent, PageComponent, ResourceEditFunctionsComponent, TileComponent],
  templateUrl: './resource-edit-page.component.html',
})
export class ResourceEditPageComponent {
  private authService = inject(AuthService);
  private modalService = inject(NgbModal);
  private resourceService = inject(ResourceService);
  private resourceTypeService = inject(ResourceTypesService);
  private route = inject(ActivatedRoute);

  id = toSignal(this.route.queryParamMap.pipe(map((params) => Number(params.get('id')))), { initialValue: 0 });
  typeId = toSignal(this.route.queryParamMap.pipe(map((params) => Number(params.get('resTypeId')))), {
    initialValue: 0,
  });
  contextId = toSignal(this.route.queryParamMap.pipe(map((params) => Number(params.get('ctx')))), { initialValue: 1 });
  resource: Signal<Resource> = this.resourceService.resource;
  resourceType: Signal<ResourceType> = this.resourceTypeService.resourceType;

  isLoading = computed(() => {
    if (this.id()) {
      this.resourceService.setIdForResource(this.id());
      return false;
    } else if (this.typeId()) {
      this.resourceTypeService.setIdForResourceType(this.typeId());
      return false;
    } else return false;
  });

  permissions = computed(() => {
    if (this.authService.restrictions().length > 0) {
      return {
        canEditResource: this.authService.hasPermission('RESOURCE', 'READ'),
        canEditResourceType: this.authService.hasPermission('RESOURCETYPE', 'READ'),
      };
    } else {
      return { canEditResource: false, canEditResourceType: false };
    }
  });

  templatesData = signal([
    {
      title: 'Instance Templates',
      entries: [
        { name: 'startJob_0.sh', description: 'startJob_0.sh', id: 0 },
        { name: 'startJob_1.sh', description: 'job 2 again', id: 1 },
      ] as TileListEntry[],
      canEdit: true,
      canDelete: true,
    },
    {
      title: 'Resource Type Templates',
      entries: [{ name: 'seg', description: 'segmentation', id: 666 }] as TileListEntry[],
      canOverwrite: false,
    },
  ]);

  add() {
    this.modalService.open('This would open a modal to add something');
  }

  doListAction($event: TileListEntryOutput) {
    switch ($event.action) {
      case EntryAction.edit:
        this.editFunction($event.id);
        return;
      case EntryAction.delete:
        this.deleteFunction($event.id);
        return;
      case EntryAction.overwrite:
        this.overwriteFunction($event.id);
        return;
    }
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