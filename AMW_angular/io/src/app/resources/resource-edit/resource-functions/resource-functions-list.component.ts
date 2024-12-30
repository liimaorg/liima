import { Component, computed, inject, input, OnDestroy, OnInit, signal } from '@angular/core';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';

import { LoadingIndicatorComponent } from '../../../shared/elements/loading-indicator.component';
import { TileComponent } from '../../../shared/tile/tile.component';

import { EntryAction, TileListEntryOutput } from '../../../shared/tile/tile-list/tile-list.component';
import { Action, AuthService } from '../../../auth/auth.service';
import { Resource } from '../../../resource/resource';
import { ResourceFunctionsService } from '../../resource-functions.service';
import { ResourceFunction } from '../../resource-function';
import { BehaviorSubject, Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { ResourceFunctionEditComponent } from './resource-function-edit.component';
import { ToastService } from '../../../shared/elements/toast/toast.service';

const RESOURCE_PERM = 'RESOURCE_AMWFUNCTION';
const RESOURCETYPE_PERM = 'RESOURCETYPE_AMWFUNCTION';

@Component({
  selector: 'app-resource-functions-list',
  standalone: true,
  imports: [LoadingIndicatorComponent, TileComponent],
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

  showLoader = signal(false);
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
      .subscribe((functionData: ResourceFunction) => console.log(functionData));
  }

  private deleteFunction(id: number) {
    this.modalService.open('This would open a modal to delete function with id:' + id);
  }

  private createFunction(functionData: ResourceFunction) {
    this.showLoader.set(true);
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
}
