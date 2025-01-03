import { Component, computed, inject, input } from '@angular/core';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';

import { LoadingIndicatorComponent } from '../../../shared/elements/loading-indicator.component';
import { TileComponent } from '../../../shared/tile/tile.component';

import { EntryAction, TileListEntryOutput } from '../../../shared/tile/tile-list/tile-list.component';
import { Action, AuthService } from '../../../auth/auth.service';
import { ResourceFunctionsService } from '../../resource-functions.service';
import { ResourceFunction } from '../../resource-function';
import { ResourceType } from '../../../resource/resource-type';
import { ResourceFunctionEditComponent } from '../../resource-edit/resource-functions/resource-function-edit.component';
import { takeUntil } from 'rxjs/operators';
import { ToastService } from '../../../shared/elements/toast/toast.service';
import { BehaviorSubject, Subject } from 'rxjs';

const RESOURCETYPE_PERM = 'RESOURCETYPE_AMWFUNCTION';

@Component({
  selector: 'app-resource-type-functions-list',
  standalone: true,
  imports: [LoadingIndicatorComponent, TileComponent],
  templateUrl: './resource-type-functions-list.component.html',
})
export class ResourceTypeFunctionsListComponent {
  private authService = inject(AuthService);
  private modalService = inject(NgbModal);
  private functionsService = inject(ResourceFunctionsService);
  private toastService = inject(ToastService);
  private destroy$ = new Subject<void>();
  private error$ = new BehaviorSubject<string>('');

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
    const modalRef = this.modalService.open(ResourceFunctionEditComponent, {
      size: 'xl',
    });
    modalRef.componentInstance.function = {
      id: null,
      name: '',
      miks: new Set<string>(),
      content: '',
    };
    modalRef.componentInstance.canEdit = this.permissions().canEdit;
    modalRef.componentInstance.saveFunction
      .pipe(takeUntil(this.destroy$))
      .subscribe((functionData: ResourceFunction) => this.createFunction(functionData));
  }

  doListAction($event: TileListEntryOutput) {
    switch ($event.action) {
      case EntryAction.edit:
        this.editFunction($event.id, false);
        return;
      case EntryAction.delete:
        this.deleteFunction($event.id);
        return;
      case EntryAction.overwrite:
        this.editFunction($event.id, true);
        return;
    }
  }

  mapListEntries(functions: ResourceFunction[]) {
    functions.sort((a, b) => (a.name < b.name ? -1 : 1));
    return functions.map((element) => ({
      name: element.name,
      description: [...element.miks].join(', '),
      id: element.id,
    }));
  }

  private editFunction(id: number, isOverwrite?: boolean) {
    const modalRef = this.modalService.open(ResourceFunctionEditComponent, {
      size: 'xl',
    });
    modalRef.componentInstance.function = this.functions().find((item) => item.id === id);
    modalRef.componentInstance.canEdit = this.permissions().canEdit;
    modalRef.componentInstance.isOverwrite = isOverwrite;
    modalRef.componentInstance.saveFunction
      .pipe(takeUntil(this.destroy$))
      .subscribe((functionData: ResourceFunction) =>
        isOverwrite ? this.overwriteFunction(functionData) : this.updateFunction(functionData),
      );
  }

  private deleteFunction(id: number) {
    this.modalService.open('This would open a modal to delete function with id:' + id);
  }

  private createFunction(functionData: ResourceFunction) {
    this.functionsService
      .createFunctionForResourceType(this.resourceType().id, functionData)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => this.toastService.success('Function saved successfully.'),
        error: (e) => this.error$.next(e.toString()),
        complete: () => {
          this.functionsService.setIdForResourceTypeFunctionList(this.resourceType().id);
        },
      });
  }

  private overwriteFunction(functionData: ResourceFunction) {
    this.functionsService
      .overwriteFunctionForResourceType(this.resourceType().id, functionData)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => this.toastService.success('Function saved successfully.'),
        error: (e) => this.error$.next(e.toString()),
        complete: () => {
          this.functionsService.setIdForResourceTypeFunctionList(this.resourceType().id);
        },
      });
  }

  private updateFunction(functionData: ResourceFunction) {
    this.functionsService
      .updateFunction(functionData)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => this.toastService.success('Function saved successfully.'),
        error: (e) => this.error$.next(e.toString()),
        complete: () => {
          this.functionsService.setIdForResourceTypeFunctionList(this.resourceType().id);
        },
      });
  }
}
