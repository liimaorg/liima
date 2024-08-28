import { Component, inject, Signal, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../auth/auth.service';
import { LoadingIndicatorComponent } from '../../shared/elements/loading-indicator.component';
import { IconComponent } from '../../shared/icon/icon.component';
import { PropertyTypesService } from './property-types.service';
import { PropertyType } from './property-type';
import { toSignal } from '@angular/core/rxjs-interop';

@Component({
  selector: 'app-property-types',
  standalone: true,
  imports: [CommonModule, IconComponent, LoadingIndicatorComponent],
  templateUrl: './property-types.component.html',
})
export class PropertyTypesComponent {
  authService = inject(AuthService);
  propertyTypeService = inject(PropertyTypesService);

  canAdd = signal<boolean>(false);
  canDelete = signal<boolean>(false);
  canDisplay = signal<boolean>(false);
  canEditName = signal<boolean>(false);
  canEditValidation = signal<boolean>(false);
  canSave = signal<boolean>(false);

  propertyTypes: Signal<PropertyType[]>;

  isLoading = true;

  properties = [
    {
      id: 1,
      propertyName: 'Example Property 1',
      encrypted: true,
      validation: 'Type: String, Length: 10',
      tags: ['tag1', 'tag2'],
    },
    {
      id: 2,
      propertyName: 'Example Property 2',
      encrypted: false,
      validation: 'Type: Number, Range: 1-100',
      tags: ['tag3'],
    },
  ];

  ngOnInit(): void {
    this.getUserPermissions();
    this.propertyTypes = this.propertyTypeService.propertyTypes;
    this.isLoading = false;
  }

  private getUserPermissions() {
    this.canAdd.set(this.authService.hasPermission('ADD_PROPTYPE', 'ALL'));
    this.canDelete.set(this.authService.hasPermission('DELETE_PROPTYPE', 'ALL'));
    this.canDisplay.set(this.authService.hasPermission('PROP_TYPE_NAME_VALUE', 'ALL'));
    this.canEditName.set(this.authService.hasPermission('EDIT_PROP_TYPE_NAME', 'ALL'));
    this.canEditValidation.set(this.authService.hasPermission('EDIT_PROP_TYPE_VALIDATION', 'ALL'));
    this.canSave.set(this.authService.hasPermission('SAVE_SETTINGS_PROPTYPE', 'ALL'));
  }

  add() {}

  edit() {}

  delete() {}
}
