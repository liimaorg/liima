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
  headers = ['Environment', 'Origin', 'Value'];

  getTitle = computed(() => {
    const infoTitle = this.property().displayName ? this.property().displayName : this.property().name;
    return this.isInfo() ? `${infoTitle}` : `Config Overview for ${this.property().name}`;
  });

  infoText = computed(() => {
    const prop = this.property();
    if (!this.isInfo()) {
      const count = prop.overwriteInfos?.length || 0;
      return count > 0
        ? `${count} environment${count > 1 ? 's' : ''} with different values`
        : 'This property is not overriden by any other child context';
    }
    if (prop.definedInContext) {
      return this.getDefinedInContextText(prop);
    } else {
      return this.getInheritedContextText(prop);
    }
  });

  dataTable = computed(() => {
    if (this.isInfo()) {
      return [
        { col1: 'TechKey', col2: this.property().name || '-' },
        { col1: 'Example value', col2: this.property().exampleValue || '-' },
        { col1: 'Default', col2: this.property().defaultValue || '-' },
        { col1: 'Comment', col2: this.property().generalComment || '-' },
        { col1: 'Machine Interpretation Key:', col2: this.property().mik || '-' },
      ];
    } else {
      return (
        this.property().overwriteInfos?.map((info) => ({
          col1: info.env || '-',
          col2: info.origin || '-',
          col3: info.val || '-',
        })) || []
      );
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
}
