import { Component, computed, inject, Signal, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../auth/auth.service';
import { LoadingIndicatorComponent } from '../../shared/elements/loading-indicator.component';
import { IconComponent } from '../../shared/icon/icon.component';
import { PropertyTypesService } from './property-types.service';
import { PropertyType } from './property-type';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { ToastService } from '../../shared/elements/toast/toast.service';
import { takeUntil } from 'rxjs/operators';
import { PropertyTypeEditComponent } from './property-type-edit.component';
import { Subject } from 'rxjs';
import { PropertyTypeDeleteComponent } from './property-type-delete.component';

@Component({
  selector: 'app-property-types',
  standalone: true,
  imports: [CommonModule, IconComponent, LoadingIndicatorComponent],
  templateUrl: './property-types.component.html',
})
export class PropertyTypesComponent {
  private authService = inject(AuthService);
  private propertyTypeService = inject(PropertyTypesService);
  private modalService = inject(NgbModal);
  private toastService = inject(ToastService);

  private destroy$ = new Subject<void>();

  canAdd = signal<boolean>(false);
  canDelete = signal<boolean>(false);
  canDisplay = signal<boolean>(false);
  canEditName = signal<boolean>(false);
  canEditValidation = signal<boolean>(false);
  canSave = signal<boolean>(false);

  propertyTypes: Signal<PropertyType[]>;
  error = signal<string>('');
  handleError = computed(() => {
    if (this.error() != '') {
      this.toastService.error(this.error());
    }
  });

  private readonly PROPERTY_TYPE = 'Property type';
  isLoading = true;

  ngOnInit(): void {
    this.getUserPermissions();
    this.getPropertyTypes();
    this.isLoading = false;
  }

  ngOnDestroy(): void {
    this.destroy$.next(undefined);
  }

  private getUserPermissions() {
    this.canAdd.set(this.authService.hasPermission('ADD_PROPTYPE', 'ALL'));
    this.canDelete.set(this.authService.hasPermission('DELETE_PROPTYPE', 'ALL'));
    this.canDisplay.set(this.authService.hasPermission('PROP_TYPE_NAME_VALUE', 'ALL'));
    this.canEditName.set(this.authService.hasPermission('EDIT_PROP_TYPE_NAME', 'ALL'));
    this.canEditValidation.set(this.authService.hasPermission('EDIT_PROP_TYPE_VALIDATION', 'ALL'));
    this.canSave.set(this.authService.hasPermission('SAVE_SETTINGS_PROPTYPE', 'ALL'));
  }

  private getPropertyTypes() {
    this.propertyTypes = this.propertyTypeService.propertyTypes;
  }

  addModal() {
    const modalRef = this.modalService.open(PropertyTypeEditComponent);
    modalRef.componentInstance.propertyType = {
      id: 0,
      name: '',
      encrypted: false,
      validationRegex: '',
      propertyTags: [],
    };
    modalRef.componentInstance.savePropertyType
      .pipe(takeUntil(this.destroy$))
      .subscribe((propertyType: PropertyType) => this.save(propertyType));
  }

  editModal(propertyType: PropertyType) {
    const modalRef = this.modalService.open(PropertyTypeEditComponent);
    modalRef.componentInstance.propertyType = propertyType;
    modalRef.componentInstance.savePropertyType
      .pipe(takeUntil(this.destroy$))
      .subscribe((propertyType: PropertyType) => this.save(propertyType));
  }

  deleteModal(propertyType: PropertyType) {
    const modalRef = this.modalService.open(PropertyTypeDeleteComponent);
    modalRef.componentInstance.propertyType = propertyType;
    modalRef.componentInstance.deletePropertyType
      .pipe(takeUntil(this.destroy$))
      .subscribe((propertyType: PropertyType) => this.delete(propertyType));
  }

  save(propertyType: PropertyType) {
    this.isLoading = true;
    this.propertyTypeService
      .save(propertyType)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (r) => this.toastService.success(`${this.PROPERTY_TYPE} saved.`),
        error: (e) => this.error.set(e),
        complete: () => {
          this.propertyTypeService.reload();
        },
      });
    this.isLoading = false;
  }

  delete(propertyType: PropertyType) {
    this.isLoading = true;
    this.propertyTypeService
      .delete(propertyType.id)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (r) => this.toastService.success(`${this.PROPERTY_TYPE} deleted.`),
        error: (e) => this.error.set(e),
        complete: () => {
          this.propertyTypeService.reload();
        },
      });
    this.isLoading = false;
  }
}
