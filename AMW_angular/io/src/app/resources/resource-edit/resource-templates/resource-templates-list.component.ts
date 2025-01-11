import { Component, computed, inject, input, OnDestroy } from '@angular/core';
import { NgbModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { LoadingIndicatorComponent } from '../../../shared/elements/loading-indicator.component';
import { TileComponent } from '../../../shared/tile/tile.component';
import { EntryAction, TileListEntryOutput } from '../../../shared/tile/tile-list/tile-list.component';
import { Action, AuthService } from '../../../auth/auth.service';
import { Resource } from '../../../resource/resource';
import { BehaviorSubject, Subject } from 'rxjs';
import { ResourceTemplatesService } from '../../../resource/resource-templates.service';
import { ResourceTemplate } from '../../../resource/resource-template';
import { ResourceTemplateDeleteComponent } from './resource-template-delete.component';
import { takeUntil } from 'rxjs/operators';
import { ToastService } from '../../../shared/elements/toast/toast.service';

const RESOURCE_PERM = 'RESOURCE_TEMPLATE';
const RESOURCETYPE_PERM = 'RESOURCETYPE_TEMPLATE';

@Component({
  selector: 'app-resource-templates-list',
  standalone: true,
  imports: [LoadingIndicatorComponent, TileComponent],
  templateUrl: './resource-templates-list.component.html',
})
export class ResourceTemplatesListComponent implements OnDestroy {
  private authService = inject(AuthService);
  private modalService = inject(NgbModal);
  private templatesService = inject(ResourceTemplatesService);
  private toastService = inject(ToastService);
  private destroy$ = new Subject<void>();
  private error$ = new BehaviorSubject<string>('');

  resource = input.required<Resource>();
  contextId = input.required<number>();
  templates = this.templatesService.resourceTemplates;

  isLoading = computed(() => {
    if (this.resource() != null) {
      this.templatesService.setIdForResourceTemplateList(this.resource().id);
      return false;
    }
  });

  permissions = computed(() => {
    if (this.authService.restrictions().length > 0 && this.resource()) {
      return {
        canShowInstanceTemplates: this.authService.hasPermission(RESOURCE_PERM, Action.READ),
        canShowSuperTypeTemplates: this.authService.hasPermission(RESOURCETYPE_PERM, Action.READ),
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
        canShowInstanceTemplates: false,
        canShowSuperTypeTemplates: false,
        canAdd: false,
        canEdit: false,
        canDelete: false,
      };
    }
  });

  templatesData = computed(() => {
    if (this.templates()?.length > 0) {
      const instanceTemplates = this.mapListEntries(
        this.templates().filter((template) => template.sourceType === 'RESOURCE'),
      );
      const typeTemplates = this.mapListEntries(
        this.templates().filter((template) => template.sourceType === 'RESOURCE_TYPE'),
      );

      const result = [];
      if (this.permissions().canShowInstanceTemplates) {
        result.push({
          title: 'Resource Instance Templates',
          entries: instanceTemplates,
          canEdit: this.permissions().canEdit,
          canDelete: this.permissions().canDelete,
        });
      }
      if (this.permissions().canShowSuperTypeTemplates) {
        result.push({
          title: 'Resource Type Templates',
          entries: typeTemplates,
          canEdit: false,
          canDelete: false,
        });
      }
      return result;
    } else return null;
  });

  ngOnDestroy(): void {
    this.destroy$.next(undefined);
  }

  doListAction($event: TileListEntryOutput) {
    switch ($event.action) {
      case EntryAction.edit:
        this.editTemplate($event.id);
        return;
      case EntryAction.delete:
        this.deleteTemplate($event.id);
        return;
    }
  }

  mapListEntries(templates: ResourceTemplate[]) {
    return templates.map((template) => ({
      name: template.name,
      description: template.targetPath,
      id: template.id,
    }));
  }

  addTemplate() {
    this.modalService.open('This would open a modal to add a new instance template');
  }

  private editTemplate(id: number) {
    this.modalService.open('This would open a modal to edit template with id: ' + id);
  }

  private deleteTemplate(id: number) {
    const modalRef: NgbModalRef = this.modalService.open(ResourceTemplateDeleteComponent);
    modalRef.componentInstance.templateId = id;
    modalRef.componentInstance.deleteTemplateId
      .pipe(takeUntil(this.destroy$))
      .subscribe((id: number) => this.removeTemplate(id));
  }

  private removeTemplate(id: number) {
    this.templatesService
      .deleteTemplate(id)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => this.toastService.success('Template deleted successfully.'),
        error: (e) => this.error$.next(e.toString()),
        complete: () => {
          this.templatesService.setIdForResourceTemplateList(this.resource().id);
        },
      });
  }
}
