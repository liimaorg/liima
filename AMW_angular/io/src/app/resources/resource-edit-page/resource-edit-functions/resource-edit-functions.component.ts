import { Component, computed, inject, input } from '@angular/core';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { ResourceFunctionsService } from './resource-functions.service';
import { LoadingIndicatorComponent } from '../../../shared/elements/loading-indicator.component';
import { TileComponent } from '../../../shared/tile/tile.component';
import { ResourceFunction } from './resource-function';
import { EntryAction, TileListEntryOutput } from '../../../shared/tile/tile-list/tile-list.component';
import { AuthService } from '../../../auth/auth.service';

@Component({
  selector: 'app-resources-edit-functions',
  standalone: true,
  imports: [LoadingIndicatorComponent, TileComponent],
  templateUrl: './resource-edit-functions.component.html',
})
export class ResourceEditFunctionsComponent {
  private authService = inject(AuthService);
  private modalService = inject(NgbModal);
  private functionsService = inject(ResourceFunctionsService);

  resourceId = input.required<number>();
  contextId = input.required<number>();
  functions = this.functionsService.functions;

  isLoading = computed(() => {
    if (this.resourceId() != null) {
      this.functionsService.setIdForFunctionList(this.resourceId());
      return false;
    }
  });

  // TODO extend AuthService for ch.puzzle.itc.mobiliar.business.security.boundary.PermissionBoundary.canCreateFunctionOfResourceOrResourceType
  permissions = computed(() => {
    if (this.authService.restrictions().length > 0) {
      return {
        canShowInstanceFunctions: this.authService.hasPermission('RESOURCE_AMWFUNCTION', 'READ'),
        canShowSuperTypeFunctions: this.authService.hasPermission('RESOURCETYPE_AMWFUNCTION', 'READ'),
        canAdd: (this.contextId() === 1 || this.contextId === null) && true,
        canEdit: (this.contextId() === 1 || this.contextId === null) && true,
        canDelete: (this.contextId() === 1 || this.contextId === null) && true,
      };
    } else {
      return {
        canShowInstanceFunctions: false,
        canShowSuperTypeFunctions: false,
        canAdd: false,
        canEdit: false,
        canDelete: false,
      };
    }
  });

  // then for editmodal it's read or edit

  functionsData = computed(() => {
    if (this.functions()?.length > 0) {
      const [instance, resource] = this.splitFunctions(this.functions());
      const result = [];
      if (this.permissions().canShowInstanceFunctions) {
        result.push({
          title: 'Resource Instance Functions',
          entries: instance,
          canEdit: this.permissions().canEdit,
          canDelete: this.permissions().canDelete,
        });
      }
      if (this.permissions().canShowSuperTypeFunctions) {
        result.push({
          title: 'Resource Type Functions',
          entries: resource,
          canOverwrite: this.permissions().canEdit,
        });
      }
      return result;
    } else return null;
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

  splitFunctions(resourceFunctions: ResourceFunction[]) {
    const [instance, resource] = [[], []];
    resourceFunctions.forEach((element) => (element.definedOnResourceType ? resource : instance).push(element));
    return [this.mapListEntries(instance), this.mapListEntries(resource)];
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
