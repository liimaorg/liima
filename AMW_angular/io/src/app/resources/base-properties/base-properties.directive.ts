import { computed, Directive, effect, inject, input, Signal, signal } from '@angular/core';
import { forkJoin, Observable, of, Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { NgbModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { Property } from '../models/property';
import { PropertyUpdate, ResourcePropertiesService } from '../services/resource-properties.service';
import { AuthService } from '../../auth/auth.service';
import { EnvironmentService } from '../../deployment/environment.service';
import { createPropertiesEditor } from '../properties-editor';
import { UnsavedPropertyChangesService } from '../services/unsaved-property-changes.service';
import { ToastService } from '../../shared/elements/toast/toast.service';
import { PropertyDescriptorService } from '../services/property-descriptor.service';
import { PropertyDeleteModalService } from '../services/property-delete-modal.service';
import { PropertyEditComponent } from '../property-edit/property-edit.component';
import { PropertyDescriptor } from '../models/property-descriptor';

@Directive()
export abstract class BasePropertiesDirective {
  contextId = input.required<number>();

  protected authService = inject(AuthService);
  protected propertiesService = inject(ResourcePropertiesService);
  protected environmentsService = inject(EnvironmentService);
  protected unsavedChangesService = inject(UnsavedPropertyChangesService);
  protected modalService = inject(NgbModal);
  protected toastService = inject(ToastService);
  protected descriptorService = inject(PropertyDescriptorService);
  deleteModalService = inject(PropertyDeleteModalService);
  protected destroy$ = new Subject<void>();

  isSaving = signal(false);
  errorMessage = signal<string | null>(null);

  // keeps the save spinner on (and drives inert-dimming) until the post-save reload of properties has finished
  protected awaitingReloadAfterSave = signal(false);

  protected editor = createPropertiesEditor(
    () => [...this.properties().filter((p) => !p.disabled)],
    () => this.getEditorOptions(),
  );

  hasChanges = this.editor.hasChanges;
  resetToken = this.editor.resetToken;

  constructor() {
    effect(() => {
      this.unsavedChangesService.setDirty(this.getUnsavedChangesKey(), this.hasChanges());
    });

    effect(() => {
      this.unsavedChangesService.discardChangesToken();
      this.resetChanges();
    });

    effect(() => {
      const entityId = this.getEntityId();
      const ctxId = this.contextId();
      if (!entityId || !ctxId) return;

      this.reloadAfterDiscardingEdits(entityId, ctxId);
    });

    effect(() => {
      if (this.awaitingReloadAfterSave() && !this.isLoading()) {
        this.awaitingReloadAfterSave.set(false);
        this.isSaving.set(false);
        this.editor.resetChanges();
      }
    });
  }

  context = computed(() => {
    return this.environmentsService.findEnvironmentById(this.environmentsService.environmentTree(), this.contextId());
  });

  onPropertyChange(propertyName: string, newValue: string) {
    this.editor.onPropertyChange(propertyName, newValue);
  }

  onPropertyReset(propertyName: string, checked: boolean) {
    this.editor.onPropertyReset(propertyName, checked);
  }

  resetChanges() {
    this.editor.resetChanges();
    this.errorMessage.set(null);
  }

  addProperty() {
    this.openEditModal(null);
  }

  saveChanges() {
    const loadedPropertyNames = new Set(this.properties().map((property) => property.name));
    const changes = new Map([...this.editor.changedProperties()].filter(([name]) => loadedPropertyNames.has(name)));
    const resets = new Set([...this.editor.resetProperties()].filter((name) => loadedPropertyNames.has(name)));
    if (changes.size === 0 && resets.size === 0) return;

    this.isSaving.set(true);
    this.errorMessage.set(null);

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
        ? this.bulkUpdateProperties(this.getEntityId(), updatedProperties, resetProperties, this.contextId())
        : of(void 0);

    forkJoin([update$]).subscribe({
      next: () => {
        this.toastService.success('Properties saved successfully');
        this.awaitingReloadAfterSave.set(true);
        this.reloadProperties(this.getEntityId(), this.contextId());
        this.afterPropertiesSaved();
      },
      error: (error) => {
        this.isSaving.set(false);
        this.errorMessage.set('Failed to save properties: ' + (error.message || 'Unknown error'));
      },
    });
  }

  protected onPropertyEdit(id: number) {
    if (id <= 0) return;
    this.openEditModal(id);
  }

  protected onPropertyDelete(id: number, deleteModal: unknown) {
    if (id <= 0) return;

    const property = this.properties().find((p) => p.descriptorId === id);
    if (!property) return;

    this.deleteModalService.showDeleteConfirmation(deleteModal, {
      id,
      name: property.displayName || property.name,
    });
  }

  confirmDelete(modal: NgbModalRef) {
    const [resourceId, resourceTypeId] = this.getDeleteParams();
    this.deleteModalService.confirmDelete(
      modal,
      resourceId,
      resourceTypeId,
      () => this.reloadAfterDiscardingEdits(this.getEntityId(), this.contextId()),
      this.destroy$,
    );
  }

  protected openEditModal(descriptorId: number | null) {
    const modalRef: NgbModalRef = this.modalService.open(PropertyEditComponent, { size: 'lg' });
    const component = modalRef.componentInstance;

    component.configure({
      descriptorId,
      canEdit: this.permissions().canUpdateProperty,
      canDecrypt: this.permissions().canDecryptProperties,
    });

    component.saveDescriptor.subscribe((updatedDescriptor: PropertyDescriptor) => {
      this.saveDescriptor(updatedDescriptor, modalRef);
    });
  }

  protected saveDescriptor(descriptor: PropertyDescriptor, modalRef: NgbModalRef) {
    const [resourceId, resourceTypeId] = this.getSaveDescriptorParams();
    this.descriptorService
      .save(descriptor, resourceId, resourceTypeId)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          this.toastService.success('Property descriptor saved successfully.');
          modalRef.close();
          this.reloadAfterDiscardingEdits(this.getEntityId(), this.contextId());
        },
        error: (err) => {
          this.toastService.error('Failed to save property descriptor: ' + err.message);
        },
      });
  }

  abstract properties: Signal<Property[]>;
  abstract permissions: Signal<{ canUpdateProperty: boolean; canDecryptProperties: boolean }>;
  abstract isLoading: Signal<boolean>;

  protected abstract getEntityId(): number;
  protected abstract getUnsavedChangesKey(): string;
  protected abstract getEditorOptions(): { includeResetsInHasChanges: boolean; unmarkResetOnChange: boolean };
  protected abstract bulkUpdateProperties(
    entityId: number,
    updatedProperties: PropertyUpdate[],
    resetProperties: PropertyUpdate[],
    contextId: number,
  ): Observable<void>;
  protected abstract afterPropertiesSaved(): void;
  protected abstract reloadProperties(entityId: number, contextId: number): void;

  private reloadAfterDiscardingEdits(entityId: number, contextId: number): void {
    this.editor.resetChanges();
    this.reloadProperties(entityId, contextId);
  }
  protected abstract getDeleteParams(): [number | undefined, number | undefined];
  protected abstract getSaveDescriptorParams(): [number | undefined, number | undefined];
}
