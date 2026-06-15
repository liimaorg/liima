import { Component, computed, inject, input, OnDestroy } from '@angular/core';
import { ResourceRelation } from '../../../models/resource-relation';
import { LoadingIndicatorComponent } from '../../../../shared/elements/loading-indicator.component';
import { TileComponent } from '../../../../shared/tile/tile.component';
import {
  EntryAction,
  TileListComponent,
  TileListEntryOutput
} from '../../../../shared/tile/tile-list/tile-list.component';
import { ResourceTemplate } from '../../../models/resource-template';
import { AuthService } from '../../../../auth/auth.service';
import { ResourceRelationsService } from '../../../services/resource-relations.service';
import { Resource } from '../../../models/resource';
import { BehaviorSubject, Subject } from 'rxjs';
import {
  ResourceTemplateEditComponent
} from '../../resource-templates/resource-template-edit/resource-template-edit.component';
import { takeUntil } from 'rxjs/operators';
import { ResourceTemplatesService } from '../../../services/resource-templates.service';
import { NgbModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { ToastService } from '../../../../shared/elements/toast/toast.service';
import {
  ResourceTemplateDeleteComponent
} from '../../resource-templates/resource-template-delete/resource-template-delete.component';

const RESOURCE_PERM = 'RESOURCE_TEMPLATE';
const RESOURCETYPE_PERM = 'RESOURCETYPE_TEMPLATE';

@Component({
  selector: 'app-relation-templates-list',
  standalone: true,
  imports: [LoadingIndicatorComponent, TileComponent, TileListComponent],
  templateUrl: './relation-templates-list.component.html',
})
export class RelationTemplatesListComponent implements OnDestroy {
  private authService = inject(AuthService);
  private resourceRelationsService = inject(ResourceRelationsService);
  private templatesService = inject(ResourceTemplatesService);
  private modalService = inject(NgbModal);
  private toastService = inject(ToastService);
  private destroy$ = new Subject<void>();
  private error$ = new BehaviorSubject<string>('');

  relation = input.required<ResourceRelation>();
  resource = input.required<Resource>();
  contextId = input.required<number>();
  templates = this.resourceRelationsService.relationTemplates;

  isLoading = computed(() => {
    if (this.relation() != null && this.resource() != null && this.contextId() != null) {
      this.resourceRelationsService.setIdsForRelationTemplates(this.resource().id, this.relation()?.id, this.contextId())
      return false;
    }
  });

  permissions = computed(() => {
    if (this.authService.restrictions().length > 0 && this.relation()) {
      return {
        canShowInstanceTemplates: this.authService.hasPermission(RESOURCE_PERM, 'READ'),
        canShowTypeTemplates: this.authService.hasPermission(RESOURCETYPE_PERM, 'READ'),
        canAdd:  (this.contextId() === 1 || this.contextId === null) &&
          (this.relation().relationType === 'consumed') &&
          this.authService.hasPermission(
            RESOURCE_PERM,
            'CREATE',
            this.resource().type,
            this.resource().resourceGroupId,
          ),
        canEdit:
          (this.contextId() === 1 || this.contextId === null) &&
          this.authService.hasPermission(
            RESOURCE_PERM,
            'UPDATE',
            this.resource().type,
            this.resource().resourceGroupId,
          ),
        canDelete:
          (this.contextId() === 1 || this.contextId === null) &&
          this.authService.hasPermission(
            RESOURCE_PERM,
            'DELETE',
            this.resource().type,
            this.resource().resourceGroupId,
          ),
      };
    } else {
      return {
        canShowInstanceTemplates: false,
        canShowTypeTemplates: false,
        canAdd: false,
        canEdit: false,
        canDelete: false,
      };
    }
  });

  templatesData = computed(() => {
    if (this.templates()?.length > 0) {
      const instanceTemplates = this.mapListEntries(
        this.templates().filter((template) => template.sourceType === 'RESOURCE_RELATION'),
      );
      const typeTemplates = this.mapListEntries(
        this.templates().filter((template) => template.sourceType === 'RESOURCE_RELATION_TYPE'),
      );

      const result = [];
      if (instanceTemplates.length > 0 && this.permissions().canShowInstanceTemplates) {
        result.push({
          title: 'Instance Templates',
          entries: instanceTemplates,
          canEdit: this.permissions().canEdit,
          canDelete: this.permissions().canDelete,
        });
      }
      if (typeTemplates.length > 0 && this.permissions().canShowTypeTemplates) {
        result.push({
          title: 'Resource Type Relationship Templates',
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
      sourceType: 'RESOURCE_RELATION',
    };
    modalRef.componentInstance.canAddOrEdit = this.permissions().canAdd;
    modalRef.componentInstance.saveTemplate
      .pipe(takeUntil(this.destroy$))
      .subscribe((templateData: ResourceTemplate) => this.createTemplate(templateData));
  }

  private createTemplate(templateData: ResourceTemplate) {
    this.resourceRelationsService
      .addRelationTemplate(templateData, this.resource().id, this.relation().id)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => this.toastService.success('Template saved successfully.'),
        error: (e) => this.error$.next(e.toString()),
        complete: () => {
          this.resourceRelationsService.setIdsForRelationTemplates(this.resource().id, this.relation().id, this.contextId());
        },
      });
  }

  protected doListAction($event: TileListEntryOutput) {
    switch ($event.action) {
      case EntryAction.edit:
        this.editTemplate($event.id);
        return;
      case EntryAction.delete:
        this.deleteTemplate($event.id);
        return;
    }
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
          this.resourceRelationsService.setIdsForRelationTemplates(this.resource().id, this.relation().id, this.contextId());
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
        templateData.sourceType = 'RESOURCE_RELATION';
        this.updateTemplate(templateData);
      });
  }

  private updateTemplate(templateData: ResourceTemplate) {
    this.resourceRelationsService
      .updateRelationTemplate(templateData, this.resource().id, this.relation().id)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => this.toastService.success('Template saved successfully.'),
        error: (e) => this.error$.next(e.toString()),
        complete: () => {
          this.resourceRelationsService.setIdsForRelationTemplates(this.resource().id, this.relation().id, this.contextId());
        },
      });
  }
}
