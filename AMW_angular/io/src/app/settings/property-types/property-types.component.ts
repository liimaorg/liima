import { Component, computed, inject, OnDestroy, OnInit, Signal, signal } from '@angular/core';
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
import { ButtonComponent } from '../../shared/button/button.component';
import { TableColumnType, TableComponent } from '../../shared/table/table.component';
import { PropertyTag } from './property-tag';

@Component({
  selector: 'app-property-types',
  standalone: true,
  imports: [CommonModule, IconComponent, LoadingIndicatorComponent, ButtonComponent, TableComponent],
  templateUrl: './property-types.component.html',
})
export class PropertyTypesComponent implements OnDestroy {
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

  propertyTypes: Signal<PropertyType[]> = this.propertyTypeService.propertyTypes;
  error = signal<string>('');
  handleError = computed(() => {
    if (this.error() != '') {
      this.toastService.error(this.error());
    }
  });

  propertyTypesTableData = computed(() =>
    this.propertyTypes().map((propertyType) => {
      return {
        id: propertyType.id,
        name: propertyType.name,
        encrypted: propertyType.encrypted,
        validationRegex: propertyType.validationRegex,
        propertyTags: propertyType.propertyTags.map((tag) => tag.name),
      };
    }),
  );

  private readonly PROPERTY_TYPE = 'Property type';
  isLoading = false;

  permissions = computed(() => {
    if (this.authService.restrictions().length > 0) {
      return {
        canAdd: this.authService.hasPermission('ADD_PROPTYPE', 'ALL'),
        canDelete: this.authService.hasPermission('DELETE_PROPTYPE', 'ALL'),
        canDisplay: this.authService.hasPermission('PROP_TYPE_NAME_VALUE', 'ALL'),
        canEditName: this.authService.hasPermission('EDIT_PROP_TYPE_NAME', 'ALL'),
        canEditValidation: this.authService.hasPermission('EDIT_PROP_TYPE_VALIDATION', 'ALL'),
        canSave: this.authService.hasPermission('SAVE_SETTINGS_PROPTYPE', 'ALL'),
      };
    } else {
      return {
        canAdd: false,
        canDelete: false,
        canDisplay: false,
        canEditName: false,
        canEditValidation: false,
        canSave: false,
      };
    }
  });

  ngOnDestroy(): void {
    this.destroy$.next(undefined);
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

  editModal(propertyTypeId: number) {
    const modalRef = this.modalService.open(PropertyTypeEditComponent);
    modalRef.componentInstance.propertyType = this.propertyTypes().find((item) => item.id === propertyTypeId);
    modalRef.componentInstance.savePropertyType
      .pipe(takeUntil(this.destroy$))
      .subscribe((propertyType: PropertyType) => this.save(propertyType));
  }

  deleteModal(propertyTypeId: number) {
    const modalRef = this.modalService.open(PropertyTypeDeleteComponent);
    modalRef.componentInstance.propertyType = this.propertyTypes().find((item) => item.id === propertyTypeId);
    modalRef.componentInstance.deletePropertyType
      .pipe(takeUntil(this.destroy$))
      .subscribe((propertyType: PropertyType) => this.delete(propertyType));
  }

  save(propertyType: PropertyType) {
    this.isLoading = true;
    if (this.canSave() && this.canEditValidation && this.canEditName) {
      this.propertyTypeService
        .save(propertyType)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: () => this.toastService.success(`${this.PROPERTY_TYPE} saved.`),
          error: (e) => this.error.set(e),
          complete: () => {
            this.propertyTypeService.reload();
          },
        });
    }
    this.isLoading = false;
  }

  delete(propertyType: PropertyType) {
    this.isLoading = true;
    this.propertyTypeService
      .delete(propertyType.id)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => this.toastService.success(`${this.PROPERTY_TYPE} deleted.`),
        error: (e) => this.error.set(e),
        complete: () => {
          this.propertyTypeService.reload();
        },
      });
    this.isLoading = false;
  }

  propertyTypesHeader(): TableColumnType<{
    id: number;
    name: string;
    encrypted: boolean;
    validationRegex: string;
    propertyTags: string[];
  }>[] {
    return [
      {
        key: 'name',
        columnTitle: 'Property Name',
      },
      {
        key: 'encrypted',
        columnTitle: 'Encrypted',
      },
      {
        key: 'validationRegex',
        columnTitle: 'Validation',
      },
      {
        key: 'propertyTags',
        columnTitle: 'Tags',
        cellType: 'badge-list',
      },
    ];
  }
}
