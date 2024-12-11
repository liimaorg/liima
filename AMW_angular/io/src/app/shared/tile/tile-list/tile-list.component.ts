import { Component, input, output } from '@angular/core';
import { ButtonComponent } from '../../button/button.component';
import { IconComponent } from '../../icon/icon.component';

export interface TileListEntry {
  name: string;
  description: string;
}

@Component({
  selector: 'app-tile-list',
  template: `
    <div class="title">{{ title() }}</div>
    <ul>
      @for (entry of data(); track entry) {
      <li class="list-entry">
        <div class="list-entry-text">
          <span>{{ entry.name }}</span>
          <span class="desc">{{ entry.description }}</span>
        </div>
        <div class="list-entry-actions">
          @if (canEdit()) {
          <app-button [size]="'sm'" [variant]="'light'" (click)="edit.emit('edit ' + entry.name)">Edit</app-button>
          } @if (canDelete()) {
          <app-button [size]="'sm'" [variant]="'light'" (click)="delete.emit('delete ' + entry.name)">
            Delete
          </app-button>
          } @if (canOverwrite()) {
          <app-button [size]="'sm'" [variant]="'light'" (click)="overwrite.emit('overwrite ' + entry.name)">
            Overwrite
          </app-button>
          }
        </div>
      </li>
      }
    </ul>
  `,
  styleUrls: ['tile-list.component.scss'],
  standalone: true,
  imports: [ButtonComponent, IconComponent],
})
export class TileListComponent {
  title = input.required<string>();
  data = input.required<TileListEntry[]>();
  canEdit = input<boolean>(false);
  canDelete = input<boolean>(false);
  canOverwrite = input<boolean>(false);
  edit = output<string>();
  delete = output<string>();
  overwrite = output<string>();
}
