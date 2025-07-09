import { Component, Input, input } from '@angular/core';
import { NgClass } from '@angular/common';

@Component({
  selector: 'app-button',
  template: `
    <button
      [type]="type()"
      class="btn"
      [ngClass]="[variantClass(), sizeClass(), additionalClasses()]"
      [disabled]="disabled()"
      [attr.data-testid]="dataTestId()"
    >
      <ng-content select="[data-cy-slot]"></ng-content>
      <ng-content></ng-content>
    </button>
  `,
  imports: [NgClass],
})
export class ButtonComponent {
  variant = input<'primary' | 'secondary' | 'danger' | 'light' | 'close' | 'link'>();
  size = input<'sm' | 'lg'>();
  additionalClasses = input<string>('');
  disabled = input<boolean>(false);
  isOutlined = input<boolean>(false);
  dataTestId = input<string>('');
  type = input<'button' | 'submit' | 'reset'>('button');

  variantClass(): string {
    return this.variant() ? (this.isOutlined() ? `btn-outline-${this.variant()}` : `btn-${this.variant()}`) : '';
  }

  sizeClass(): string {
    return this.size() ? `btn-${this.size()}` : '';
  }
}
