import { Component, computed, input, output } from '@angular/core';
import { Property } from '../models/property';
import { PropertyFieldComponent } from '../property-field/property-field.component';
import {
  PropertiesResetToggleAction,
  PropertiesValidationChangeAction,
  PropertiesValueChangeAction,
} from '../models/properties-action';
import { NgTemplateOutlet } from '@angular/common';

@Component({
  selector: 'app-properties-list',
  standalone: true,
  imports: [PropertyFieldComponent, NgTemplateOutlet],
  templateUrl: './properties-list.component.html',
  styleUrl: './properties-list.component.scss',
})
export class PropertiesListComponent {
  properties = input.required<Property[]>();
  canEdit = input<boolean>(false);
  canDecrypt = input<boolean>(false);
  canDelete = input<boolean>(false);
  mode = input<'resource' | 'resourceType'>('resource');
  resetToken = input<number>(0);

  valueChange = output<PropertiesValueChangeAction>();
  resetToggled = output<PropertiesResetToggleAction>();
  validationChanged = output<PropertiesValidationChangeAction>();

  listProperties = computed(() => {
    const props = this.properties();
    const mid = Math.ceil(props.length / 2);
    return { left: props.slice(0, mid), right: props.slice(mid) };
  });
}
