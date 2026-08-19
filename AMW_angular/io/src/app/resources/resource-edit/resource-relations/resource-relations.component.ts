import {
  Component,
  computed,
  inject,
  linkedSignal,
  signal,
  Signal,
  TemplateRef,
  ViewChild,
  effect,
} from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { Observable, of, forkJoin } from 'rxjs';
import { NgOptionComponent, NgSelectComponent } from '@ng-select/ng-select';
import { finalize } from 'rxjs/operators';
import { TileComponent } from 'src/app/shared/tile/tile.component';
import { LoadingIndicatorComponent } from 'src/app/shared/elements/loading-indicator.component';
import { ButtonComponent } from 'src/app/shared/button/button.component';
import { IconComponent } from 'src/app/shared/icon/icon.component';
import { ModalHeaderComponent } from 'src/app/shared/modal-header/modal-header.component';
import { ResourceService } from '../../services/resource.service';
import { ResourceTypesService } from '../../services/resource-types.service';
import { PropertyUpdate } from '../../services/resource-properties.service';
import { GroupedResourceRelations, ResourceRelation, UnresolvedRelation } from '../../models/resource-relation';
import { Resource } from '../../models/resource';
import { ResourceType } from '../../models/resource-type';
import { Property } from '../../models/property';
import { RelationGroupItem, RelationGroupComponent } from '../../relation-group/relation-group.component';
import { PropertiesPanelComponent } from '../../properties-panel/properties-panel.component';
import { PropertiesListComponent } from '../../properties-list/properties-list.component';
import { BaseRelationsDirective, NODE_FILTERED_PROPERTIES } from '../../base-relations/base-relations.directive';
import { RelationActiveApplicationsComponent } from './relation-active-applications/relation-active-applications.component';
import { ResourceActivationService, ResourceActivation } from '../../services/resource-activation.service';
import { ResourceRelationTemplatesListComponent } from './resource-relation-templates-list/resource-relation-templates-list.component';

@Component({
  selector: 'app-resource-relations',
  standalone: true,
  imports: [
    TileComponent,
    LoadingIndicatorComponent,
    RelationGroupComponent,
    ButtonComponent,
    FormsModule,
    PropertiesPanelComponent,
    PropertiesListComponent,
    NgOptionComponent,
    NgSelectComponent,
    IconComponent,
    RouterLink,
    ModalHeaderComponent,
    RelationActiveApplicationsComponent,
    ResourceRelationTemplatesListComponent,
  ],
  templateUrl: './resource-relations.component.html',
  styleUrl: './resource-relations.component.scss',
})
export class ResourceRelationsComponent extends BaseRelationsDirective {
  private resourceService = inject(ResourceService);
  private resourceTypesService = inject(ResourceTypesService);
  private resourceActivationService = inject(ResourceActivationService);
  resource: Signal<Resource | null> = this.resourceService.resource;

  @ViewChild('addRelationModal') addRelationModal!: TemplateRef<void>;
  @ViewChild('removeRelationConfirmation') removeRelationConfirmation!: TemplateRef<void>;
  @ViewChild('setRuntimeModal') setRuntimeModal!: TemplateRef<void>;

  availableRuntimes = signal<Resource[]>([]);
  selectedRuntimeId = signal<number | null>(null);
  isSettingRuntime = signal(false);

  resourceTypes = signal<ResourceType[]>([]);
  childResourceTypes = signal<ResourceType[]>([]);
  availableResourceGroups = signal<Resource[]>([]);
  selectedResourceTypeId = signal<number | null>(null);
  selectedChildTypeId = signal<number | null>(null);
  selectedResourceGroupId = signal<number | null>(null);
  addAsProvided = signal<boolean>(false);
  isAddingRelation = signal<boolean>(false);

  originalActiveAppIds = signal<number[]>([]);
  currentActiveAppIds = signal<number[]>([]);
  isSavingActivations = signal(false);

  protected groupedRelations: Signal<GroupedResourceRelations> = this.relationsService.relations;
  protected isLoadingRelations = this.relationsService.isLoadingRelations;
  protected isLoadingProperties = this.relationsService.isLoadingRelationProperties;

  protected hasRelations = computed(() => {
    const g = this.groupedRelations();
    return g.runtime.length + g.consumed.length + g.provided.length + g.unresolved.length > 0;
  });

  isResource = computed(
    () =>
      this.availableResourceGroups().length > 0 ||
      (this.selectedResourceTypeId() && this.childResourceTypes().length === 0),
  );

  isApplicationType = computed(
    () => this.resource()?.type === '"APPLICATION"' || this.resource()?.type === 'APPLICATION',
  );

  isApplicationServer = computed(
    () => this.resource()?.type === 'APPLICATIONSERVER' || this.resource()?.type === '"APPLICATIONSERVER"',
  );

  canSetRuntime = computed(
    () =>
      this.isApplicationServer() &&
      this.contextId() === 1 &&
      this.permissions().canUpdateProperty &&
      this.groupedRelations().runtime.length === 0,
  );

  hasNewerRelease = computed(() => {
    const res = this.resource();
    const releases = this.resourceService.releasesForResourceGroup();
    if (!res?.release || !releases?.length) return false;
    return releases.some((r) => (r.release ?? '') > (res.release ?? ''));
  });

  protected permissions = computed(() => {
    if (this.authService.restrictions().length > 0) {
      return {
        canUpdateProperty: this.authService.hasPermission(
          'RESOURCE',
          'UPDATE',
          undefined,
          this.resource()?.resourceTypeId,
          this.context()?.name,
        ),
        canDecryptProperties: this.authService.hasPermission(
          'RESOURCE_PROPERTY_DECRYPT',
          'ALL',
          undefined,
          this.resource()?.id,
          this.context()?.name,
        ),
      };
    } else {
      return { canUpdateProperty: false, canDecryptProperties: false };
    }
  });

  // set by url, input, onItemSelected and onReleaseChange
  protected activeRelationId = linkedSignal(() => {
    const relId = this.selectedRelationId();
    if (relId != null && relId > 0) return relId;
    const g = this.groupedRelations();
    return [...g.runtime, ...g.consumed, ...g.provided][0]?.id ?? null;
  });

  // needed for css highlighting on switching releases
  selectedItemKey = computed<number | null>(() => {
    const relId = this.activeRelationId();
    if (relId == null) return null;
    const g = this.groupedRelations();
    const all = [...g.runtime, ...g.consumed, ...g.provided];
    if (all.some((r) => r.id === relId)) return relId;
    for (const r of all) {
      if (r.availableReleases?.some((ar) => ar.relationId === relId)) {
        return r.id;
      }
    }
    return relId;
  });

  // resolves selected releation and handles releases fallback
  selectedRelation = computed<ResourceRelation | null>(() => {
    const relId = this.activeRelationId();
    if (relId == null) return null;
    const g = this.groupedRelations();
    const all = [...g.runtime, ...g.consumed, ...g.provided];
    const direct = all.find((r) => r.id === relId);
    if (direct) return direct;
    for (const r of all) {
      const release = r.availableReleases?.find((ar) => ar.relationId === relId);
      if (release) {
        return { ...r, id: relId, slaveId: release.slaveId, relatedResourceRelease: release.releaseName };
      }
    }
    return null;
  });

  selectedRelationIdForRelease = computed(() => this.selectedRelation()?.id ?? null);

  protected isApplicationServerToNodeRelation = computed(() => {
    const resource = this.resource();
    const relation = this.selectedRelation();
    if (!resource || !relation) return false;

    const isAppServer = resource.type === 'APPLICATIONSERVER' || resource.type === '"APPLICATIONSERVER"';
    const isNode = relation.type === 'NODE' || relation.type === '"NODE"';

    return isAppServer && isNode;
  });

  protected isNode = computed(() => {
    const type = this.resource()?.type;
    return this.getCleanType(type) === 'NODE';
  });

  /**
   * Cleans a resource type string by removing surrounding quotes and converting to uppercase.
   * @param type - The resource type string, possibly undefined
   * @returns The cleaned type string in uppercase, or undefined if input is undefined
   */
  protected getCleanType(type: string | undefined): string | undefined {
    return type?.replace(/"/g, '').toUpperCase();
  }

  protected properties = computed<Property[]>(() => {
    const props = this.relationsService.relationProperties;
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
    disabled: this.contextId() !== 1,
  }));

  hasActivationChanges = computed(() => {
    const original = this.originalActiveAppIds();
    const current = this.currentActiveAppIds();
    if (original.length !== current.length) return true;
    const originalSet = new Set(original);
    for (const id of current) {
      if (!originalSet.has(id)) return true;
    }
    return false;
  });

  override hasChanges = computed(() => this.editor.hasChanges() || this.hasActivationChanges());

  protected entityId = computed(() => this.resource()?.id);

  constructor() {
    super();
    effect(() => {
      const activations = this.resourceActivationService.activations();
      const activeIds = activations
        .filter((a: ResourceActivation) => a.active)
        .map((a: ResourceActivation) => a.resourceGroupId);
      this.originalActiveAppIds.set(activeIds);
      this.currentActiveAppIds.set(activeIds);
    });
  }

  onActivationChange(appIds: number[]) {
    this.currentActiveAppIds.set(appIds);
  }

  onReleaseChange(relationId: number) {
    this.activeRelationId.set(relationId);
    this.setQueryParamForRelationId(relationId);
  }

  showAddRelationModal(): void {
    this.selectedResourceTypeId.set(null);
    this.selectedChildTypeId.set(null);
    this.selectedResourceGroupId.set(null);
    this.addAsProvided.set(false);
    this.childResourceTypes.set([]);
    this.availableResourceGroups.set([]);

    if (this.isDefaultResourceType()) {
      this.loadAllResourceTypes();
    } else {
      this.loadRelatedResourceTypes();
    }

    this.modalService.open(this.addRelationModal, { size: 'lg' });
  }

  showSetRuntimeModal(): void {
    this.selectedRuntimeId.set(null);
    this.availableRuntimes.set([]);
    this.loadAvailableRuntimes();
    this.modalService.open(this.setRuntimeModal, { size: 'md' });
  }

  private loadAvailableRuntimes(): void {
    this.resourceService.getGroupsForTypeName('RUNTIME').subscribe({
      next: (runtimes) => {
        // Filter out already set runtime if any
        const currentRuntimeId = this.groupedRelations().runtime[0]?.slaveId;
        this.availableRuntimes.set(runtimes.filter((r) => r.id !== currentRuntimeId));
      },
      error: (err) => {
        console.error('Failed to load runtimes:', err);
        this.toastService.error('Failed to load available runtimes.');
      },
    });
  }

  setRuntime(): void {
    const runtimeId = this.selectedRuntimeId();
    if (!runtimeId) {
      this.toastService.error('Please select a runtime.');
      return;
    }

    this.isSettingRuntime.set(true);
    this.relationsService.addResourceRelation(this.entityId()!, runtimeId, false).subscribe({
      next: () => {
        this.toastService.success('Runtime set successfully.');
        this.modalService.dismissAll();
        this.isSettingRuntime.set(false);
        this.reloadRelation(this.entityId()!);
      },
      error: (err) => {
        console.error('Failed to set runtime:', err);
        this.toastService.error('Failed to set runtime: ' + (err.message || 'Unknown error'));
        this.isSettingRuntime.set(false);
      },
    });
  }

  onResourceTypeChange(typeId: number | null): void {
    this.selectedResourceTypeId.set(typeId);
    this.selectedChildTypeId.set(null);
    this.selectedResourceGroupId.set(null);
    this.childResourceTypes.set([]);
    this.availableResourceGroups.set([]);
    if (!typeId) return;

    const type = this.resourceTypes().find((t) => t.id === typeId);
    if (type?.hasChildren && type.children?.length > 0) {
      this.childResourceTypes.set(type.children);
    }
    this.loadResourceGroups(typeId);
  }

  onChildTypeChange(childTypeId: number | null): void {
    this.selectedChildTypeId.set(childTypeId);
    this.selectedResourceGroupId.set(null);
    this.availableResourceGroups.set([]);
    if (childTypeId) {
      this.loadResourceGroups(childTypeId);
    }
  }

  addRelation(): void {
    const groupId = this.selectedResourceGroupId();
    if (!groupId) {
      this.toastService.error('Please select a resource.');
      return;
    }

    const res = this.resource();
    if (res?.release) {
      const selectedGroup = this.availableResourceGroups().find((g) => g.id === groupId);
      if (selectedGroup?.releases?.length) {
        const firstRelease = selectedGroup.releases[0];
        const currentReleaseName = res.release;
        if (firstRelease?.release && firstRelease.release > currentReleaseName) {
          if (
            !confirm(
              `The selected resource does not exist for the release ${currentReleaseName}. Are you sure you want to add it for this release?`,
            )
          ) {
            return;
          }
        }
      }
    }

    this.isAddingRelation.set(true);
    this.relationsService.addResourceRelation(this.entityId()!, groupId, this.addAsProvided()).subscribe({
      next: () => {
        this.toastService.success('Relation added successfully.');
        this.modalService.dismissAll();
        this.isAddingRelation.set(false);
        this.reloadRelation(this.entityId()!);
      },
      error: (err) => {
        console.error('Failed to add relation:', err);
        this.toastService.error('Failed to add relation: ' + (err.message || 'Unknown error'));
        this.isAddingRelation.set(false);
      },
    });
  }

  showRemoveRelationConfirmation(): void {
    this.modalService.open(this.removeRelationConfirmation).result.then(
      () => this.removeRelation(),
      () => {},
    );
  }

  // ==================== OVERRIDE METHODS - Save/Reset ====================
  override saveChanges() {
    const propertyChanges = this.editor.changedProperties();
    const propertyResets = this.editor.resetProperties();
    const hasPropertyChanges = propertyChanges.size > 0 || propertyResets.size > 0;
    const hasActivationChanges = this.hasActivationChanges();

    if ((!hasPropertyChanges && !hasActivationChanges) || this.hasValidationErrors()) return;

    this.isSaving.set(true);
    this.errorMessage.set(null);
    this.successMessage.set(null);

    let propertyUpdate$: Observable<void> = of(void 0);
    if (hasPropertyChanges) {
      const updatedProperties: PropertyUpdate[] = Array.from(propertyChanges.entries()).map(([name, value]) => ({
        name,
        value,
      }));
      const resetProperties: PropertyUpdate[] = Array.from(propertyResets.entries()).map(([name, value]) => ({
        name,
        value,
      }));
      propertyUpdate$ = this.bulkUpdateProperties(
        this.getRelationId()!,
        updatedProperties,
        resetProperties,
        this.contextId(),
      );
    }

    let activationUpdate$: Observable<void> = of(void 0);
    if (hasActivationChanges && this.isApplicationServerToNodeRelation()) {
      this.isSavingActivations.set(true);
      activationUpdate$ = this.resourceActivationService.updateActivations(
        this.entityId()!,
        this.getRelationId()!,
        this.contextId(),
        { activeResourceGroupIds: this.currentActiveAppIds() },
      );
    }

    forkJoin([propertyUpdate$, activationUpdate$])
      .pipe(
        finalize(() => {
          this.isSaving.set(false);
          this.isSavingActivations.set(false);
        }),
      )
      .subscribe({
        next: () => {
          this.successMessage.set('Changes saved successfully');
          if (hasPropertyChanges) {
            this.reloadProperties(this.entityId()!, this.getRelationId()!, this.contextId());
            this.afterPropertiesSaved();
            this.editor.resetChanges();
          }
          if (hasActivationChanges) {
            this.resourceActivationService.setRelationParams(this.entityId()!, this.getRelationId()!, this.contextId());
          }
          setTimeout(() => this.successMessage.set(null), 3000);
        },
        error: (error) => {
          this.errorMessage.set('Failed to save changes: ' + (error.message || 'Unknown error'));
        },
      });
  }

  override resetChanges() {
    super.resetChanges();
    this.currentActiveAppIds.set(this.originalActiveAppIds());
  }

  protected reloadRelation(entityId: number): void {
    this.relationsService.setIdForResourceRelations(entityId);
  }

  protected reloadProperties(entityId: number, relationId: number, contextId: number): void {
    this.relationsService.setIdsForRelationProperties(entityId, relationId, contextId);
  }

  protected bulkUpdateProperties(
    relationId: number,
    updatedProperties: PropertyUpdate[],
    resetProperties: PropertyUpdate[],
    contextId: number,
  ): Observable<void> {
    return this.relationsService.bulkUpdateResourceRelationProperties(
      this.entityId()!,
      relationId,
      updatedProperties,
      resetProperties,
      contextId,
    );
  }

  protected afterPropertiesSaved(): void {
    const changes = this.editor.changedProperties();
    if (changes.has('relationName')) {
      this.reloadRelation(this.entityId()!);
    }
  }

  protected getUnsavedChangesKey(): string {
    return 'resource-relation-properties';
  }

  protected getEditorOptions(): { includeResetsInHasChanges: boolean; unmarkResetOnChange: boolean } {
    return {
      includeResetsInHasChanges: true,
      unmarkResetOnChange: true,
    };
  }

  protected hasIdentifierProperty() {
    const rel = this.selectedRelation();
    return rel != null && rel.relationType === 'consumed' && rel.type !== 'RUNTIME';
  }

  protected toUnresolvedItem(unresolved: UnresolvedRelation): RelationGroupItem {
    return {
      key: `${unresolved.type}::${unresolved.name}`,
      name: unresolved.name,
      type: unresolved.type,
      unresolved: true,
    };
  }

  private loadResourceGroups(typeId: number): void {
    this.resourceService.getGroupsForType({ id: typeId } as ResourceType).subscribe({
      next: (groups) => this.availableResourceGroups.set(groups),
      error: (err) => {
        console.error('Failed to load resource groups:', err);
        this.toastService.error('Failed to load resource groups.');
      },
    });
  }

  private isDefaultResourceType(): boolean {
    const type = this.resource()?.type;
    const defaultTypes = ['APPLICATION', 'APPLICATIONSERVER', 'NODE', 'RUNTIME'];
    const cleanType = this.getCleanType(type);
    if (!cleanType) return false;
    return defaultTypes.includes(cleanType);
  }

  private loadRelatedResourceTypes(): void {
    const unresolvedTypes = new Set(this.groupedRelations().unresolved.map((u) => u.type));

    if (unresolvedTypes.size === 0) {
      return;
    }

    this.resourceTypesService.getAllResourceTypes().subscribe({
      next: (types) => {
        const filteredTypes = types.filter((t) => unresolvedTypes.has(t.name));
        this.resourceTypes.set(filteredTypes);
      },
      error: (err) => {
        console.error('Failed to load resource types:', err);
        this.toastService.error('Failed to load resource types.');
      },
    });
  }

  private loadAllResourceTypes(): void {
    this.resourceTypesService.getRootResourceTypes().subscribe({
      next: (types) => this.resourceTypes.set(types),
      error: (err) => {
        console.error('Failed to load resource types:', err);
        this.toastService.error('Failed to load resource types.');
      },
    });
  }

  private removeRelation(): void {
    const rel = this.selectedRelation();
    if (!rel) return;

    this.relationsService.removeResourceRelation(this.entityId()!, rel.id).subscribe({
      next: () => {
        this.toastService.success('Relation removed successfully.');
        this.reloadRelation(this.entityId()!);
      },
      error: (err) => {
        console.error('Failed to remove relation:', err);
        this.toastService.error('Failed to remove relation: ' + (err.message || 'Unknown error'));
      },
    });
  }
}
