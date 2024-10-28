import { Component, input, output } from '@angular/core';
import { NgClass } from '@angular/common';

@Component({
  selector: 'app-button',
  template: `
    <button type="button" class="btn" [ngClass]="class()" [disabled]="disabled()" (click)="clicked.emit()">
      <ng-content select="[data-cy-slot]"></ng-content>
      <ng-content></ng-content>
    </button>
  `,
  standalone: true,
  imports: [NgClass],
})
export class ButtonComponent {
  class = input<string>('');
  disabled = input<boolean>(false);
  clicked = output<void>();
}
