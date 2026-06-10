import { Component, computed, inject, linkedSignal, signal, Signal, TemplateRef, ViewChild } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { Observable, Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { NgSelectModule } from '@ng-select/ng-select';
import { TileComponent } from '../../../shared/tile/tile.component';
import { LoadingIndicatorComponent } from '../../../shared/elements/loading-indicator.component';
import { ButtonComponent } from '../../../shared/button/button.component';
import { IconComponent } from '../../../shared/icon/icon.component';
import { ModalHeaderComponent } from '../../../shared/modal-header/modal-header.component';
import { ResourceTypesService } from '../../services/resource-types.service';
import { PropertyUpdate } from '../../services/resource-properties.service';
import { GroupedResourceRelations, UnresolvedRelation } from '../../models/resource-relation';
import { ResourceType } from '../../models/resource-type';
import { Property } from '../../models/property';
import { RelationGroupItem, RelationGroupComponent } from '../../relation-group/relation-group.component';
import { PropertiesListComponent } from '../../properties-list/properties-list.component';
import { PropertiesPanelComponent } from '../../properties-panel/properties-panel.component';
import { BaseRelationsDirective, NODE_FILTERED_PROPERTIES } from '../../base-relations/base-relations.directive';
import { ResourceTemplateEditComponent } from '../../resource-edit/resource-templates/resource-template-edit/resource-template-edit.component';
import { ResourceTemplate } from '../../models/resource-template';
import { ResourceTemplatesService } from '../../services/resource-templates.service';

@Component({
  selector: 'app-resource-type-relations',
  standalone: true,
  imports: [
    TileComponent,
    LoadingIndicatorComponent,
    RelationGroupComponent,
    ButtonComponent,
    IconComponent,
    PropertiesListComponent,
    PropertiesPanelComponent,
    ModalHeaderComponent,
    FormsModule,
    NgSelectModule,
  ],
  templateUrl: './resource-type-relations.component.html',
  styleUrl: './resource-type-relations.component.scss',
})
export class ResourceTypeRelationsComponent extends BaseRelationsDirective {
  private resourceTypeService = inject(ResourceTypesService);
  // TODO move to relation-template-component or to base-relations
  private templatesService = inject(ResourceTemplatesService);
  resourceType: Signal<ResourceType> = this.resourceTypeService.resourceType;

  @ViewChild('addTypeRelationModal') addTypeRelationModal!: TemplateRef<void>;
  @ViewChild('removeTypeRelationConfirmation') removeTypeRelationConfirmation!: TemplateRef<void>;

  allResourceTypes = signal<ResourceType[]>([]);
  selectedSlaveTypeId = signal<number | null>(null);
  isAddingRelation = signal<boolean>(false);

  protected groupedRelations: Signal<GroupedResourceRelations> = this.relationsService.typeRelations;
  protected isLoadingRelations = this.relationsService.isLoadingTypeRelations;

  protected permissions = computed(() => {
    if (this.authService.restrictions().length > 0) {
      return {
        canUpdateProperty: this.authService.hasPermission(
          'RESOURCETYPE',
          'UPDATE',
          this.resourceType()?.name,
          null,
          this.context()?.name,
        ),
        canDecryptProperties: this.authService.hasPermission(
          'RESOURCETYPE_PROPERTY_DECRYPT',
          'ALL',
          this.resourceType()?.name,
          null,
          this.context()?.name,
        ),
      };
    } else {
      return { canUpdateProperty: false, canDecryptProperties: false };
    }
  });

  // Resource types currently only have unresolved relations (no runtime/consumed/provided).
  protected hasRelations = computed(() => this.groupedRelations().unresolved.length > 0);

  protected activeRelationId = linkedSignal(() => {
    const id = this.selectedRelationId();
    if (id != null && id > 0) return id;
    return this.groupedRelations().unresolved[0]?.resRelTypeId ?? null;
  });

  selectedRelation = computed<UnresolvedRelation | null>(() => {
    const id = this.activeRelationId();
    if (id == null) return null;
    return this.groupedRelations().unresolved.find((r) => r.resRelTypeId === id) ?? null;
  });

  protected isLoadingProperties = this.relationsService.isLoadingTypeRelationProperties;

  protected properties = computed<Property[]>(() => {
    const props = this.relationsService.typeRelationProperties;
    const result: Property[] = [];
    if (this.hasIdentifierProperty()) {
      result.push(this.relationIdentifier());
    }
    const allProps = props();
    const rel = this.selectedRelation();
    if (rel?.type === 'NODE' && !this.isEnvironment()) {
      result.push(...allProps.filter((p) => !NODE_FILTERED_PROPERTIES.includes(p.name)));
    } else {
      result.push(...allProps);
    }
    return result;
  });

  protected entityId = computed(() => this.resourceType()?.id);

  protected reloadRelation(entityId: number): void {
    this.relationsService.setIdForResourceTypeRelations(entityId);
  }

  protected reloadProperties(entityId: number, relationId: number, contextId: number): void {
    this.relationsService.setIdsForTypeRelationProperties(entityId, relationId, contextId);
  }

  protected bulkUpdateProperties(
    relationId: number,
    updatedProperties: PropertyUpdate[],
    resetProperties: PropertyUpdate[],
    contextId: number,
  ): Observable<void> {
    return this.relationsService.bulkUpdateResourceTypeRelationProperties(
      this.entityId(),
      relationId,
      updatedProperties,
      resetProperties,
      contextId,
    );
  }

  protected afterPropertiesSaved(): void {
    const changes = this.editor.changedProperties();
    if (changes.has('relationName')) {
      this.reloadRelation(this.entityId());
    }
  }

  protected getUnsavedChangesKey(): string {
    return 'resourceType-relation-properties';
  }

  protected getEditorOptions(): { includeResetsInHasChanges: boolean; unmarkResetOnChange: boolean } {
    return {
      includeResetsInHasChanges: true,
      unmarkResetOnChange: true,
    };
  }

  protected hasIdentifierProperty() {
    return this.selectedRelation() != null;
  }

  protected toUnresolvedItem(unresolved: UnresolvedRelation): RelationGroupItem {
    return {
      key: unresolved.resRelTypeId,
      name: unresolved.name,
      type: unresolved.type,
      unresolved: true,
      identifier: unresolved.identifier,
    };
  }

  relationIdentifier = computed<Property>(() => ({
    name: 'relationName',
    displayName: `Relation name`,
    value: this.selectedRelation()?.identifier || '',
    replacedValue: '',
    generalComment: '',
    valueComment: 'specialProperty',
    descriptorId: -1,
    context: 'Global',
    nullable: true,
    optional: true,
    disabled: !this.permissions().canUpdateProperty,
  }));

  showAddTypeRelationModal(): void {
    this.selectedSlaveTypeId.set(null);
    this.resourceTypeService.getAllResourceTypes().subscribe({
      next: (types) => {
        const currentId = this.entityId();
        this.allResourceTypes.set(types.filter((t) => t.id !== currentId));
      },
      error: (err) => {
        console.error('Failed to load resource types:', err);
        this.toastService.error('Failed to load resource types.');
      },
    });
    this.modalService.open(this.addTypeRelationModal, { size: 'lg' });
  }

  addTypeRelation(): void {
    const slaveTypeId = this.selectedSlaveTypeId();
    if (!slaveTypeId) {
      this.toastService.error('Please select a resource type.');
      return;
    }

    this.isAddingRelation.set(true);
    this.relationsService.addResourceTypeRelation(this.entityId(), slaveTypeId).subscribe({
      next: () => {
        this.toastService.success('Relation added successfully.');
        this.modalService.dismissAll();
        this.isAddingRelation.set(false);
        this.reloadRelation(this.entityId());
      },
      error: (err) => {
        console.error('Failed to add type relation:', err);
        this.toastService.error('Failed to add relation: ' + (err.message || 'Unknown error'));
        this.isAddingRelation.set(false);
      },
    });
  }

  showRemoveTypeRelationConfirmation(): void {
    this.modalService.open(this.removeTypeRelationConfirmation).result.then(
      () => this.removeTypeRelation(),
      () => {},
    );
  }

  private removeTypeRelation(): void {
    const rel = this.selectedRelation();
    if (!rel || !rel.resRelTypeId) return;

    this.relationsService.removeResourceTypeRelation(this.entityId(), rel.resRelTypeId).subscribe({
      next: () => {
        this.toastService.success('Relation removed successfully.');
        this.reloadRelation(this.entityId());
      },
      error: (err) => {
        console.error('Failed to remove type relation:', err);
        this.toastService.error('Failed to remove relation: ' + (err.message || 'Unknown error'));
      },
    });
  }

  /**
   * TODO move to relation-template-component or base-relations
   */
  protected addRelationTemplate() {
    const modalRef = this.modalService.open(ResourceTemplateEditComponent, {
      size: 'xl',
    });
    modalRef.componentInstance.template = {
      id: null,
      relatedResourceIdentifier: this.selectedRelation()?.identifier || '',
      name: '',
      targetPath: '',
      targetPlatforms: [],
      fileContent: '',
      sourceType: 'RESOURCE_TYPE',
    };
    modalRef.componentInstance.canAddOrEdit = this.authService.hasPermission(
      'RESOURCETYPE_TEMPLATE',
      'UPDATE',
      this.resourceType()?.name,
      null,
    );
    modalRef.componentInstance.saveTemplate
      .pipe(takeUntil(this.destroy$))
      .subscribe((templateData: ResourceTemplate) => this.createRelationTemplate(templateData));
  }

  private createRelationTemplate(templateData: ResourceTemplate) {
    this.templatesService
      .addRelationTemplate(templateData, this.selectedRelation()?.resRelTypeId)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => this.toastService.success('Template saved successfully.'),
      });
  }
}
