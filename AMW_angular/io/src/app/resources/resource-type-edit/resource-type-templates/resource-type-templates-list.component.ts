import { Component, computed, inject, input, OnDestroy } from '@angular/core';
import { NgbModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { LoadingIndicatorComponent } from '../../../shared/elements/loading-indicator.component';
import { TileComponent } from '../../../shared/tile/tile.component';
import { EntryAction, TileListEntryOutput } from '../../../shared/tile/tile-list/tile-list.component';
import { Action, AuthService } from '../../../auth/auth.service';
import { BehaviorSubject, Subject } from 'rxjs';
import { ResourceTemplatesService } from '../../../resource/resource-templates.service';
import { ResourceTemplate } from '../../../resource/resource-template';
import { ResourceType } from '../../../resource/resource-type';
import { takeUntil } from 'rxjs/operators';
import { ToastService } from '../../../shared/elements/toast/toast.service';
import { ResourceTemplateDeleteComponent } from '../../resource-edit/resource-templates/resource-template-delete.component';

const RESOURCETYPE_PERM = 'RESOURCETYPE_TEMPLATE';

@Component({
  selector: 'app-resource-type-templates-list',
  standalone: true,
  imports: [LoadingIndicatorComponent, TileComponent],
  templateUrl: './resource-type-templates-list.component.html',
})
export class ResourceTypeTemplatesListComponent implements OnDestroy {
  private authService = inject(AuthService);
  private modalService = inject(NgbModal);
  private templatesService = inject(ResourceTemplatesService);
  private toastService = inject(ToastService);
  private destroy$ = new Subject<void>();
  private error$ = new BehaviorSubject<string>('');

  resourceType = input.required<ResourceType>();
  contextId = input.required<number>();
  templates = this.templatesService.resourceTypeTemplates;

  isLoading = computed(() => {
    if (this.resourceType() != null) {
      this.templatesService.setIdForResourceTypeTemplateList(this.resourceType().id);
      return false;
    }
  });

  permissions = computed(() => {
    if (this.authService.restrictions().length > 0 && this.resourceType()) {
      return {
        canShowSuperTypeTemplates: this.authService.hasPermission(RESOURCETYPE_PERM, Action.READ),
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
        canShowSuperTypeTemplates: false,
        canAdd: false,
        canEdit: false,
        canDelete: false,
      };
    }
  });

  templatesData = computed(() => {
    if (this.templates()?.length > 0) {
      const typeTemplates = this.mapListEntries(this.templates());
      return [
        {
          title: 'Resource Type Templates',
          entries: typeTemplates,
          canEdit: this.permissions().canEdit,
          canDelete: this.permissions().canDelete,
        },
      ];
    } else {
      return null;
    }
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
    this.modalService.open('This would open a modal to add a new resource type template');
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
          this.templatesService.setIdForResourceTypeTemplateList(this.resourceType().id);
        },
      });
  }
}
