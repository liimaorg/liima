import { Component, input } from '@angular/core';
import { NgClass } from '@angular/common';

@Component({
  selector: 'app-button',
  template: `
    <button
      type="button"
      class="btn"
      [ngClass]="[variantClass(), sizeClass(), additionalClasses()]"
      [disabled]="disabled()"
      [attr.data-cy]="dataCy()"
    >
      <ng-content select="[data-cy-slot]"></ng-content>
      <ng-content></ng-content>
    </button>
  `,
  standalone: true,
  imports: [NgClass],
})
export class ButtonComponent {
  variant = input<'primary' | 'secondary' | 'danger' | 'light' | 'close' | 'link'>();
  size = input<'sm' | 'lg'>();
  additionalClasses = input<string>('');
  disabled = input<boolean>(false);
  dataCy = input<string>('');

  variantClass(): string {
    return this.variant() ? `btn-${this.variant()}` : '';
  }

  sizeClass(): string {
    return this.size() ? `btn-${this.size()}` : '';
  }
}
