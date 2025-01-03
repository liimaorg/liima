import { Component, computed, inject, Signal, signal } from '@angular/core';
import { LoadingIndicatorComponent } from '../../shared/elements/loading-indicator.component';
import { PageComponent } from '../../layout/page/page.component';
import { ActivatedRoute } from '@angular/router';
import { map } from 'rxjs/operators';
import { toSignal } from '@angular/core/rxjs-interop';
import { EntryAction, TileListEntry, TileListEntryOutput } from '../../shared/tile/tile-list/tile-list.component';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { TileComponent } from '../../shared/tile/tile.component';
import { AuthService } from '../../auth/auth.service';
import { ResourceType } from '../../resource/resource-type';
import { ResourceTypesService } from '../../resource/resource-types.service';
import { ResourceTypeFunctionsListComponent } from './resource-type-functions/resource-type-functions-list.component';

@Component({
  selector: 'app-resource-type-edit',
  standalone: true,
  imports: [LoadingIndicatorComponent, PageComponent, TileComponent, ResourceTypeFunctionsListComponent],
  templateUrl: './resource-type-edit.component.html',
})
export class ResourceTypeEditComponent {
  private authService = inject(AuthService);
  private modalService = inject(NgbModal);
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
        this.edit($event.id);
        return;
      case EntryAction.delete:
        this.delete($event.id);
        return;
      case EntryAction.overwrite:
        this.overwrite($event.id);
        return;
    }
  }

  private edit(id: number) {
    this.modalService.open('This would open a modal to edit with id:' + id);
  }

  private delete(id: number) {
    this.modalService.open('This would open a modal to delete with id:' + id);
  }

  private overwrite(id: number) {
    this.modalService.open('This would open a modal to overwrite with id:' + id);
  }
}
