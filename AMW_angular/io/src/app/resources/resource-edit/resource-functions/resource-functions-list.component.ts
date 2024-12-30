import { Component, computed, inject, input, OnDestroy } from '@angular/core';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';

import { LoadingIndicatorComponent } from '../../../shared/elements/loading-indicator.component';
import { TileComponent } from '../../../shared/tile/tile.component';

import { EntryAction, TileListEntryOutput } from '../../../shared/tile/tile-list/tile-list.component';
import { Action, AuthService } from '../../../auth/auth.service';
import { Resource } from '../../../resource/resource';
import { ResourceFunctionsService } from '../../resource-functions.service';
import { ResourceFunction } from '../../resource-function';
import { FunctionEditComponent } from '../../../settings/functions/function-edit.component';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';

const RESOURCE_PERM = 'RESOURCE_AMWFUNCTION';
const RESOURCETYPE_PERM = 'RESOURCETYPE_AMWFUNCTION';

@Component({
  selector: 'app-resource-functions-list',
  standalone: true,
  imports: [LoadingIndicatorComponent, TileComponent],
  templateUrl: './resource-functions-list.component.html',
})
export class ResourceFunctionsListComponent implements OnDestroy {
  private authService = inject(AuthService);
  private modalService = inject(NgbModal);
  private functionsService = inject(ResourceFunctionsService);
  private destroy$ = new Subject<void>();

  resource = input.required<Resource>();
  contextId = input.required<number>();
  functions = this.functionsService.functions;

  isLoading = computed(() => {
    if (this.resource() != null) {
      this.functionsService.setIdForResourceFunctionList(this.resource().id);
      return false;
    }
  });

  permissions = computed(() => {
    if (this.authService.restrictions().length > 0 && this.resource()) {
      return {
        canShowInstanceFunctions: this.authService.hasPermission(RESOURCE_PERM, Action.READ),
        canShowSuperTypeFunctions: this.authService.hasPermission(RESOURCETYPE_PERM, Action.READ),
        canAdd:
          (this.contextId() === 1 || this.contextId === null) &&
          this.authService.hasResourceGroupPermission(RESOURCE_PERM, Action.CREATE, this.resource().resourceGroupId),
        canEdit:
          (this.contextId() === 1 || this.contextId === null) &&
          this.authService.hasResourceGroupPermission(RESOURCE_PERM, Action.UPDATE, this.resource().resourceGroupId),
        canDelete:
          (this.contextId() === 1 || this.contextId === null) &&
          this.authService.hasResourceGroupPermission(RESOURCE_PERM, Action.DELETE, this.resource().resourceGroupId),
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

  functionsData = computed(() => {
    if (this.functions()?.length > 0) {
      const [instance, resource] = this.splitFunctions(this.functions());
      const result = [];
      if (this.permissions().canShowInstanceFunctions) {
        result.push({
          title: 'Resource Instance Functions',
          entries: instance,
          canEdit: this.permissions().canEdit || this.permissions().canShowInstanceFunctions, // fixme old gui used the `Edit`-link also for only viewing a function
          canDelete: this.permissions().canDelete,
        });
      }
      if (this.permissions().canShowSuperTypeFunctions) {
        result.push({
          title: 'Resource Type Functions',
          entries: resource,
          canOverwrite: this.permissions().canEdit || this.permissions().canShowInstanceFunctions, // fixme old gui used the `Edit`-link also for only viewing a function
        });
      }
      return result;
    } else return null;
  });

  ngOnDestroy(): void {
    this.destroy$.next(undefined);
  }

  add() {
    const modalRef = this.modalService.open(FunctionEditComponent, {
      size: 'xl',
    });
    modalRef.componentInstance.function = {
      id: null,
      name: '',
      content: '',
    };
    modalRef.componentInstance.canManage = this.permissions().canEdit;
    modalRef.componentInstance.saveFunction
      .pipe(takeUntil(this.destroy$))
      .subscribe((functionData: ResourceFunction) => console.log(functionData));
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
    return functions.map((element) => ({
      name:
        element.name +
        (element.definedOnResourceType
          ? ` (Defined on ${element.functionOriginResourceName})`
          : element.isOverwritingFunction
          ? ` (Overwrite function from ${element.overwrittenParentName})`
          : ''),
      description: element.miks.join(', '),
      id: element.id,
    }));
  }

  splitFunctions(resourceFunctions: ResourceFunction[]) {
    const [instance, resource] = [[], []];
    resourceFunctions.sort((a, b) => (a.name < b.name ? -1 : 1));
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
