import { Component, computed, inject, input, OnDestroy } from '@angular/core';
import { ResourceRelation, UnresolvedRelation } from '../../../models/resource-relation';
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
import { BehaviorSubject, Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { ResourceTemplatesService } from '../../../services/resource-templates.service';
import { NgbModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { ToastService } from '../../../../shared/elements/toast/toast.service';
import {
  ResourceTemplateDeleteComponent
} from '../../../resource-edit/resource-templates/resource-template-delete/resource-template-delete.component';
import {
  ResourceTemplateEditComponent
} from '../../../resource-edit/resource-templates/resource-template-edit/resource-template-edit.component';
import { ResourceType } from '../../../models/resource-type';

const RESOURCETYPE_PERM = 'RESOURCETYPE_TEMPLATE';

@Component({
  selector: 'app-resource-type-relation-templates-list',
  standalone: true,
  imports: [LoadingIndicatorComponent, TileComponent, TileListComponent],
  templateUrl: './resource-type-relation-templates-list.component.html',
})
export class ResourceTypeRelationTemplatesListComponent implements OnDestroy {
  private authService = inject(AuthService);
  private resourceRelationsService = inject(ResourceRelationsService);
  private templatesService = inject(ResourceTemplatesService);
  private modalService = inject(NgbModal);
  private toastService = inject(ToastService);
  private destroy$ = new Subject<void>();
  private error$ = new BehaviorSubject<string>('');

  relation = input.required<UnresolvedRelation>();
  resourceType = input.required<ResourceType>();
  contextId = input.required<number>();
  templates = this.resourceRelationsService.resourceTypeRelationTemplates;

  isLoading = computed(() => {
    if (this.relation() != null && this.resourceType() != null && this.contextId() != null) {
      this.resourceRelationsService.setIdsForResourceTypeRelationTemplates(this.resourceType().id, this.relation()?.resRelTypeId)
      return false;
    }
  });

  permissions = computed(() => {
    if (this.authService.restrictions().length > 0 && this.relation()) {
      return {
        canShowTypeTemplates: this.authService.hasPermission(RESOURCETYPE_PERM, 'READ'),
        canAdd:
          (this.contextId() === 1 || this.contextId === null) &&
          this.authService.hasPermission(RESOURCETYPE_PERM, 'CREATE', this.resourceType().name),
        canEdit:
          (this.contextId() === 1 || this.contextId === null) &&
          this.authService.hasPermission(RESOURCETYPE_PERM, 'UPDATE', this.resourceType().name),
        canDelete:
          (this.contextId() === 1 || this.contextId === null) &&
          this.authService.hasPermission(RESOURCETYPE_PERM, 'DELETE', this.resourceType().name),
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
          title: 'Resource Type Relationship Templates',
          entries: typeTemplates,
          canEdit: this.permissions().canEdit,
          canDelete: this.permissions().canDelete,
        }
      ];
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
      sourceType: 'RESOURCE_RELATION_TYPE',
    };
    modalRef.componentInstance.canAddOrEdit = this.permissions().canAdd;
    modalRef.componentInstance.saveTemplate
      .pipe(takeUntil(this.destroy$))
      .subscribe((templateData: ResourceTemplate) => this.createTemplate(templateData));
  }

  private createTemplate(templateData: ResourceTemplate) {
    this.resourceRelationsService
      .addResourceTypeRelationTemplate(templateData, this.resourceType().id, this.relation().resRelTypeId)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => this.toastService.success('Template saved successfully.'),
        error: (e) => this.error$.next(e.toString()),
        complete: () => {
          this.resourceRelationsService.setIdsForResourceTypeRelationTemplates(this.resourceType().id, this.relation().resRelTypeId);
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
          this.resourceRelationsService.setIdsForResourceTypeRelationTemplates(this.resourceType().id, this.relation().resRelTypeId);
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
        templateData.sourceType = 'RESOURCE_RELATION_TYPE';
        this.updateTemplate(templateData);
      });
  }

  private updateTemplate(templateData: ResourceTemplate) {
    this.resourceRelationsService
      .updateResourceTypeRelationTemplate(templateData, this.resourceType().id, this.relation().resRelTypeId)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => this.toastService.success('Template saved successfully.'),
        error: (e) => this.error$.next(e.toString()),
        complete: () => {
          this.resourceRelationsService.setIdsForResourceTypeRelationTemplates(this.resourceType().id, this.relation().resRelTypeId);
        },
      });  }
}
