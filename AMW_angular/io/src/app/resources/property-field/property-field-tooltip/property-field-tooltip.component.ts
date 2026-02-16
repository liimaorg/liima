import { Component, computed, input } from '@angular/core';
import { Property } from '../../models/property';
import { TooltipComponent } from '../../../shared/tooltip/tooltip.component';
import { IconComponent } from '../../../shared/icon/icon.component';

@Component({
  selector: 'app-property-field-tooltip',
  standalone: true,
  imports: [TooltipComponent, IconComponent],
  templateUrl: './property-field-tooltip.component.html',
  styleUrl: './property-field-tooltip.component.scss',
})
export class PropertyFieldTooltipComponent {
  property = input.required<Property>();
  isInfo = input.required<boolean>();

  infoText = computed(() => {
    const prop = this.property();

    if (prop.definedInContext) {
      return this.getDefinedInContextText(prop);
    } else {
      return this.getInheritedContextText(prop);
    }
  });

  private getDefinedInContextText(prop: Property): string {
    if (
      this.isValueEmpty(prop.value) ||
      this.isValueEmpty(prop.replacedValue) ||
      this.isValueEmpty(prop.originOfValue)
    ) {
      return '';
    }

    if (prop.encrypted) {
      return `Replaces value of '${prop.originOfValue}'`;
    }
    return `Replaces value '${prop.replacedValue}' of '${prop.originOfValue}'`;
  }

  private getInheritedContextText(prop: Property): string {
    if (this.hasReplacedValue(prop) || this.isValueEmpty(prop.originOfValue)) {
      return '';
    }

    return `Defined in '${prop.originOfValue}'`;
  }

  private isValueEmpty(value: string | null | undefined): boolean {
    return value == null || value === '';
  }

  private hasReplacedValue(prop: Property): boolean {
    return prop.replacedValue != null && prop.replacedValue !== '';
  }

  dataTable = computed(() => {
    if (this.isInfo()) {
      return [
        { label: 'TechKey', value: this.property().name },
        { label: 'Example value', value: this.property().exampleValue },
        { label: 'Default', value: this.property().defaultValue },
        { label: 'Comment', value: this.property().generalComment },
        { label: 'Machine Interpretation Key:', value: this.property().mik },
      ];
    }
  });
}
