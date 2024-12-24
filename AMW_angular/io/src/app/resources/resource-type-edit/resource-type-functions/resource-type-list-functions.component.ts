import { Component, computed, inject, input } from '@angular/core';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';

import { LoadingIndicatorComponent } from '../../../shared/elements/loading-indicator.component';
import { TileComponent } from '../../../shared/tile/tile.component';

import { EntryAction, TileListEntryOutput } from '../../../shared/tile/tile-list/tile-list.component';
import { Action, AuthService } from '../../../auth/auth.service';
import { ResourceFunctionsService } from '../../resource-functions.service';
import { ResourceFunction } from '../../resource-function';
import { ResourceType } from '../../../resource/resource-type';

const RESOURCETYPE_PERM = 'RESOURCETYPE_AMWFUNCTION';

@Component({
  selector: 'app-resource-type-list-functions',
  standalone: true,
  imports: [LoadingIndicatorComponent, TileComponent],
  templateUrl: './resource-type-list-functions.component.html',
})
export class ResourceTypeListFunctionsComponent {
  private authService = inject(AuthService);
  private modalService = inject(NgbModal);
  private functionsService = inject(ResourceFunctionsService);

  resourceType = input.required<ResourceType>();
  contextId = input.required<number>();
  functions = this.functionsService.functionsForType;

  isLoading = computed(() => {
    if (this.resourceType() != null) {
      this.functionsService.setIdForResourceTypeFunctionList(this.resourceType().id);
      return false;
    }
  });

  permissions = computed(() => {
    if (this.authService.restrictions().length > 0 && this.resourceType()) {
      return {
        canShowInstanceFunctions: this.authService.hasPermission(RESOURCETYPE_PERM, Action.READ),
        canAdd:
          (this.contextId() === 1 || this.contextId === null) &&
          this.authService.hasResourceTypePermission(RESOURCETYPE_PERM, Action.CREATE, this.resourceType().name),
        canEdit:
          (this.contextId() === 1 || this.contextId === null) &&
          this.authService.hasResourceTypePermission(RESOURCETYPE_PERM, Action.UPDATE, this.resourceType().name),
        canDelete:
          (this.contextId() === 1 || this.contextId === null) &&
          this.authService.hasResourceTypePermission(RESOURCETYPE_PERM, Action.DELETE, this.resourceType().name),
      };
    } else {
      return {
        canShowInstanceFunctions: false,
        canAdd: false,
        canEdit: false,
        canDelete: false,
      };
    }
  });

  functionsData = computed(() => {
    if (this.functions()?.length > 0) {
      if (this.permissions().canShowInstanceFunctions) {
        const entries = this.mapListEntries(this.functions());
        return [
          {
            title: 'Type Functions',
            entries: entries,
            canEdit: this.permissions().canEdit || this.permissions().canShowInstanceFunctions, // fixme old gui used the `Edit`-link also for only viewing a function
            canDelete: this.permissions().canDelete,
          },
        ];
      } else return null;
    }
  });

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

  mapListEntries(functions: ResourceFunction[]) {
    return functions.map((element) => ({ name: element.name, description: element.miks.join(', '), id: element.id }));
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
