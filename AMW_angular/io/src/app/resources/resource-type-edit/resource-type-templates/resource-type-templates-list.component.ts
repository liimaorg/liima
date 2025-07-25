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
import { ResourceTemplateEditComponent } from '../../resource-edit/resource-templates/resource-template-edit.component';

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
        canShowTypeTemplates: this.authService.hasPermission(RESOURCETYPE_PERM, Action.READ),
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
        canShowTypeTemplates: false,
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
    return templates
      .sort((a, b) => a.name.localeCompare(b.name))
      .map((template) => ({
        name: template.name,
        description: template.targetPath,
        id: template.id,
      }));
  }

  addTemplate() {
    const modalRef = this.modalService.open(ResourceTemplateEditComponent, {
      size: 'xl',
    });
    modalRef.componentInstance.template = {
      id: null,
      relatedResourceIdentifier: '',
      name: '',
      targetPath: '',
      targetPlatforms: [],
      fileContent: '',
      sourceType: 'RESOURCE_TYPE',
    };
    modalRef.componentInstance.canAddOrEdit = this.permissions().canAdd;
    modalRef.componentInstance.saveTemplate
      .pipe(takeUntil(this.destroy$))
      .subscribe((templateData: ResourceTemplate) => this.createTemplate(templateData));
  }

  private createTemplate(templateData: ResourceTemplate) {
    this.templatesService
      .addResourceTypeTemplate(templateData, this.resourceType().id)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => this.toastService.success('Template saved successfully.'),
        error: (e) => this.error$.next(e.toString()),
        complete: () => {
          this.templatesService.setIdForResourceTypeTemplateList(this.resourceType().id);
        },
      });
  }

  private editTemplate(id: number) {
    const modalRef = this.modalService.open(ResourceTemplateEditComponent, {
      size: 'xl',
    });
    modalRef.componentInstance.template = this.templates()?.find((item) => item.id === id);
    modalRef.componentInstance.canAddOrEdit = this.permissions().canEdit;
    modalRef.componentInstance.saveTemplate
      .pipe(takeUntil(this.destroy$))
      .subscribe((templateData: ResourceTemplate) => {
        templateData.sourceType = 'RESOURCE_TYPE';
        this.updateTemplate(templateData);
      });
  }

  private updateTemplate(templateData: ResourceTemplate) {
    this.templatesService
      .updateResourceTypeTemplate(templateData, this.resourceType().id)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => this.toastService.success('Template saved successfully.'),
        error: (e) => this.error$.next(e.toString()),
        complete: () => {
          this.templatesService.setIdForResourceTypeTemplateList(this.resourceType().id);
        },
      });
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
