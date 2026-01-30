import { Component, computed, inject, input, OnDestroy, OnInit } from '@angular/core';
import { NgbModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';

import { BehaviorSubject, Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { AuthService } from 'src/app/auth/auth.service';
import { Resource } from 'src/app/resources/models/resource';
import { ResourceFunction } from 'src/app/resources/models/resource-function';
import { ResourceFunctionsService } from 'src/app/resources/services/resource-functions.service';
import { LoadingIndicatorComponent } from 'src/app/shared/elements/loading-indicator.component';
import { ToastService } from 'src/app/shared/elements/toast/toast.service';
import { EntryAction, TileListComponent, TileListEntryOutput } from 'src/app/shared/tile/tile-list/tile-list.component';
import { TileComponent } from 'src/app/shared/tile/tile.component';
import { ResourceFunctionDeleteComponent } from './../resource-function-delete/resource-function-delete.component';
import { ResourceFunctionEditComponent } from './../resource-function-edit/resource-function-edit.component';

const RESOURCE_PERM = 'RESOURCE_AMWFUNCTION';
const RESOURCETYPE_PERM = 'RESOURCETYPE_AMWFUNCTION';

@Component({
  selector: 'app-resource-functions-list',
  standalone: true,
  imports: [LoadingIndicatorComponent, TileComponent, TileListComponent],
  templateUrl: './resource-functions-list.component.html',
})
export class ResourceFunctionsListComponent implements OnInit, OnDestroy {
  private authService = inject(AuthService);
  private modalService = inject(NgbModal);
  private functionsService = inject(ResourceFunctionsService);
  private toastService = inject(ToastService);
  private destroy$ = new Subject<void>();
  private error$ = new BehaviorSubject<string>('');

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
        canShowInstanceFunctions: this.authService.hasPermission(RESOURCE_PERM, 'READ'),
        canShowSuperTypeFunctions: this.authService.hasPermission(RESOURCETYPE_PERM, 'READ'),
        canAdd:
          this.contextId() === 1 &&
          this.authService.hasPermission(
            RESOURCE_PERM,
            'CREATE',
            this.resource().type,
            this.resource().resourceGroupId,
          ),
        canEdit: this.authService.hasPermission(
          RESOURCE_PERM,
          'UPDATE',
          this.resource().type,
          this.resource().resourceGroupId,
        ),
        canDelete:
          this.contextId() === 1 &&
          this.authService.hasPermission(
            RESOURCE_PERM,
            'DELETE',
            this.resource().type,
            this.resource().resourceGroupId,
          ),
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
          canEdit: this.permissions().canEdit || this.permissions().canShowInstanceFunctions,
          canDelete: this.permissions().canDelete,
        });
      }
      if (this.permissions().canShowSuperTypeFunctions) {
        result.push({
          title: 'Resource Type Functions',
          entries: resource,
          canOverwrite: this.permissions().canEdit && this.contextId() === 1,
        });
      }
      return result;
    } else return null;
  });

  ngOnInit(): void {
    this.error$.pipe(takeUntil(this.destroy$)).subscribe((msg) => {
      msg !== '' ? this.toastService.error(msg) : null;
    });
  }

  ngOnDestroy(): void {
    this.destroy$.next(undefined);
  }

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
    return functions.map((element) => ({
      name:
        element.name +
        (element.definedOnResourceType
          ? ` (Defined on ${element.functionOriginResourceName})`
          : element.isOverwritingFunction
            ? ` (Overwrite function from ${element.overwrittenParentName})`
            : ''),
      description: [...element.miks].join(', '),
      id: element.id,
    }));
  }

  splitFunctions(resourceFunctions: ResourceFunction[]) {
    const [instance, resource] = [[], []];
    resourceFunctions.sort((a, b) => (a.name < b.name ? -1 : 1));
    resourceFunctions.forEach((element) => (element.definedOnResourceType ? resource : instance).push(element));
    return [this.mapListEntries(instance), this.mapListEntries(resource)];
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
    const modalRef: NgbModalRef = this.modalService.open(ResourceFunctionDeleteComponent);
    modalRef.componentInstance.functionId = id;
    modalRef.componentInstance.deleteFunctionId
      .pipe(takeUntil(this.destroy$))
      .subscribe((id: number) => this.removeFunction(id));
  }

  private createFunction(functionData: ResourceFunction) {
    this.functionsService
      .createFunctionForResource(this.resource().id, functionData)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => this.toastService.success('Function saved successfully.'),
        error: (e) => this.error$.next(e.toString()),
        complete: () => {
          this.functionsService.setIdForResourceFunctionList(this.resource().id);
        },
      });
  }

  private overwriteFunction(functionData: ResourceFunction) {
    this.functionsService
      .overwriteFunctionForResource(this.resource().id, functionData)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => this.toastService.success('Function saved successfully.'),
        error: (e) => this.error$.next(e.toString()),
        complete: () => {
          this.functionsService.setIdForResourceFunctionList(this.resource().id);
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
          this.functionsService.setIdForResourceFunctionList(this.resource().id);
        },
      });
  }

  private removeFunction(id: number) {
    this.functionsService
      .deleteFunction(id)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => this.toastService.success('Function deleted successfully.'),
        error: (e) => this.error$.next(e.toString()),
        complete: () => {
          this.functionsService.setIdForResourceFunctionList(this.resource().id);
        },
      });
  }
}
