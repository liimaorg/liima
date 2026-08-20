import { computed, Directive, effect, inject, input, Signal, signal, WritableSignal } from '@angular/core';
import { forkJoin, Observable, of, Subject } from 'rxjs';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { Property } from '../models/property';
import { AuthService } from '../../auth/auth.service';
import { EnvironmentService } from '../../deployment/environment.service';
import { createPropertiesEditor } from '../properties-editor';
import { UnsavedPropertyChangesService } from '../services/unsaved-property-changes.service';
import { ToastService } from '../../shared/elements/toast/toast.service';
import { ResourceRelationsService } from '../services/resource-relations.service';
import { ActivatedRoute, Router } from '@angular/router';
import { GroupedResourceRelations, ResourceRelation, UnresolvedRelation } from '../models/resource-relation';
import { RelationGroupItem } from '../relation-group/relation-group.component';
import { PropertyUpdate } from '../services/resource-properties.service';
import { finalize } from 'rxjs/operators';
import { ConfirmDialogService } from '../../shared/confirm-dialog/confirm-dialog.service';

export const NODE_FILTERED_PROPERTIES = ['hostName', 'active'];

@Directive()
export abstract class BaseRelationsDirective {
  contextId = input.required<number>();
  selectedRelationId = input<number | null>(null);

  protected authService = inject(AuthService);
  protected relationsService = inject(ResourceRelationsService);
  protected environmentsService = inject(EnvironmentService);
  protected unsavedChangesService = inject(UnsavedPropertyChangesService);
  protected modalService = inject(NgbModal);
  protected toastService = inject(ToastService);
  protected route = inject(ActivatedRoute);
  protected router = inject(Router);
  protected confirmDialogService = inject(ConfirmDialogService);

  protected destroy$ = new Subject<void>();

  isSaving = signal(false);
  errorMessage = signal<string | null>(null);
  successMessage = signal<string | null>(null);

  protected editor = createPropertiesEditor(
    () => [...this.properties().filter((p) => !p.disabled)],
    () => this.getEditorOptions(),
  );

  hasChanges = this.editor.hasChanges;
  resetToken = this.editor.resetToken;

  runtimeItems = computed(() => this.groupedRelations().runtime.map((r) => this.toItem(r)));
  consumedItems = computed(() => this.groupedRelations().consumed.map((r) => this.toItem(r)));
  providedItems = computed(() => this.groupedRelations().provided.map((r) => this.toItem(r)));
  unresolvedItems = computed(() => this.groupedRelations().unresolved.map((u) => this.toUnresolvedItem(u)));

  constructor() {
    effect(() => {
      this.unsavedChangesService.setDirty(this.getUnsavedChangesKey(), this.hasChanges());
    });

    effect(() => {
      this.unsavedChangesService.discardChangesToken();
      this.resetChanges();
    });

    effect(() => {
      const entityId = this.entityId();
      if (entityId) {
        this.reloadRelation(entityId);
      }
    });

    effect(() => {
      const entityId = this.entityId();
      const relationId = this.getRelationId();
      const ctxId = this.contextId();
      if (!entityId || !ctxId || !relationId) return;

      this.reloadProperties(entityId, relationId, ctxId);
    });
  }

  context = computed(() => {
    return this.environmentsService.findEnvironmentById(this.environmentsService.environmentTree(), this.contextId());
  });

  protected isEnvironment = computed(() => {
    const ctx = this.context();
    return ctx != null && (!ctx.children || ctx.children.length === 0);
  });

  getRelationId(): number | null {
    return this.activeRelationId();
  }

  setQueryParamForRelationId(relationId: number) {
    void this.router.navigate([], {
      relativeTo: this.route,
      queryParams: { rel: relationId },
      queryParamsHandling: 'merge',
    });
  }

  saveChanges() {
    const changes = this.editor.changedProperties();
    const resets = this.editor.resetProperties();
    if (changes.size === 0 && resets.size === 0) return;

    this.isSaving.set(true);
    this.errorMessage.set(null);
    this.successMessage.set(null);

    const updatedProperties: PropertyUpdate[] = Array.from(changes.entries()).map(([name, value]) => ({
      name,
      value,
    }));

    const resetProperties: PropertyUpdate[] = Array.from(resets.entries()).map(([name, value]) => ({
      name,
      value,
    }));

    const update$ =
      updatedProperties.length + resetProperties.length
        ? this.bulkUpdateProperties(this.getRelationId()!, updatedProperties, resetProperties, this.contextId())
        : of(void 0);

    forkJoin([update$])
      .pipe(
        finalize(() => {
          this.isSaving.set(false);
        }),
      )
      .subscribe({
        next: () => {
          this.successMessage.set('Properties saved successfully');
          this.reloadProperties(this.entityId()!, this.getRelationId()!, this.contextId());
          this.afterPropertiesSaved();
          this.editor.resetChanges();
          setTimeout(() => this.successMessage.set(null), 3000);
        },
        error: (error) => {
          this.errorMessage.set('Failed to save properties: ' + (error.message || 'Unknown error'));
        },
      });
  }

  onPropertyChange(propertyName: string, newValue: string) {
    this.editor.onPropertyChange(propertyName, newValue);
  }

  onPropertyReset(propertyName: string, checked: boolean) {
    this.editor.onPropertyReset(propertyName, checked);
  }

  onItemSelected(item: RelationGroupItem) {
    const id = typeof item.key === 'number' ? item.key : null;
    if (id == null) return;
    this.switchRelation(id);
  }

  protected switchRelation(relationId: number): void {
    if (relationId === this.getRelationId()) return;

    if (this.hasChanges()) {
      void this.confirmDialogService
        .confirm('You have unsaved changes. Discard them and switch relation?', { confirmLabel: 'Discard' })
        .then((proceed) => {
          if (!proceed) return;
          this.resetChanges();
          this.activeRelationId.set(relationId);
          this.setQueryParamForRelationId(relationId);
        });
      return;
    }

    this.activeRelationId.set(relationId);
    this.setQueryParamForRelationId(relationId);
  }

  toItem(relation: ResourceRelation): RelationGroupItem {
    return {
      key: relation.id,
      name: relation.relatedResourceName,
      type: relation.type,
      release: relation.relatedResourceRelease,
      identifier: relation.identifier,
    };
  }

  resetChanges() {
    this.editor.resetChanges();
    this.errorMessage.set(null);
    this.successMessage.set(null);
  }

  protected abstract properties: Signal<Property[]>;
  protected permissions!: Signal<{ canUpdateProperty: boolean; canDecryptProperties: boolean }>;
  protected abstract isLoadingRelations: Signal<boolean>;
  protected abstract isLoadingProperties: Signal<boolean>;
  protected abstract groupedRelations: Signal<GroupedResourceRelations>;
  protected abstract hasRelations: Signal<boolean>;
  protected abstract activeRelationId: WritableSignal<number | null>;

  protected abstract entityId: Signal<number | undefined>;
  protected abstract getUnsavedChangesKey(): string;
  protected abstract getEditorOptions(): { includeResetsInHasChanges: boolean; unmarkResetOnChange: boolean };
  protected abstract bulkUpdateProperties(
    entityId: number,
    updatedProperties: PropertyUpdate[],
    resetProperties: PropertyUpdate[],
    contextId: number,
  ): Observable<void>;
  protected abstract afterPropertiesSaved(): void;
  protected abstract hasIdentifierProperty(): boolean;
  protected abstract reloadRelation(entityId: number): void;
  protected abstract reloadProperties(entityId: number, relationId: number, contextId: number): void;
  protected abstract toUnresolvedItem(unresolved: UnresolvedRelation): RelationGroupItem;
}
