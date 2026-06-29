import { Component, computed, input } from '@angular/core';
import { IconComponent } from '../icon/icon.component';

@Component({
  selector: 'app-sortable-icon',
  template: ` <app-icon icon="caret-{{ direction() }}-fill"></app-icon> `,
  styles: [],
  imports: [IconComponent],
})
export class SortableIconComponent {
  sortDirection = input.required<string>();
  direction = computed(() => (this.sortDirection() === 'ASC' ? 'up' : 'down'));
}

type SortDirection = 'ASC' | 'DESC';
