import { Component, input, output } from '@angular/core';
import { Property } from '../models/property';
import { PropertyFieldComponent } from '../property-field/property-field.component';
import { PropertiesResetToggleAction, PropertiesValueChangeAction } from '../models/properties-action';

@Component({
  selector: 'app-properties-list',
  standalone: true,
  imports: [PropertyFieldComponent],
  templateUrl: './properties-list.component.html',
  styleUrl: './properties-list.component.scss',
})
export class PropertiesListComponent {
  properties = input.required<Property[]>();
  specialProperties = input<Property[]>([]);
  canEdit = input<boolean>(false);
  canDelete = input<boolean>(false);
  mode = input<'resource' | 'resourceType'>('resource');

  valueChange = output<PropertiesValueChangeAction>();
  resetToggled = output<PropertiesResetToggleAction>();
}
