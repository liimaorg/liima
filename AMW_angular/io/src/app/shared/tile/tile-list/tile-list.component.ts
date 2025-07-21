import { Component, input, output } from '@angular/core';
import { ButtonComponent } from '../../button/button.component';

export interface TileListEntry {
  id: number;
  name: string;
  description: string;
}

export enum EntryAction {
  edit = 'edit',
  delete = 'delete',
  overwrite = 'overwrite',
}

export interface TileListEntryOutput {
  action: EntryAction;
  id: number;
}

@Component({
  selector: 'app-tile-list',
  template: `
    <div class="title">{{ title() }}</div>

    @if (!data() || data().length <= 0) {
      <div class="no-content">
        <span>No {{ title() }} for this resource</span>
      </div>
    }
    <ul>
      @for (entry of data(); track entry) {
        <li class="list-entry">
          <div class="list-entry-text">
            <span>{{ entry.name }}</span>
            <span class="desc">{{ entry.description }}</span>
          </div>
          <div class="list-entry-actions">
            @if (canEdit()) {
              <app-button
                [size]="'sm'"
                [variant]="'light'"
                (click)="edit.emit({ action: EntryAction.edit, id: entry.id })"
                >Edit
              </app-button>
            }
            @if (canDelete()) {
              <app-button
                [size]="'sm'"
                [variant]="'light'"
                (click)="delete.emit({ action: EntryAction.delete, id: entry.id })"
              >
                Delete
              </app-button>
            }
            @if (canOverwrite()) {
              <app-button
                [size]="'sm'"
                [variant]="'light'"
                (click)="overwrite.emit({ action: EntryAction.overwrite, id: entry.id })"
              >
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
  imports: [ButtonComponent],
})
export class TileListComponent {
  title = input.required<string>();
  data = input.required<TileListEntry[]>();
  canEdit = input<boolean>(false);
  canDelete = input<boolean>(false);
  canOverwrite = input<boolean>(false);
  edit = output<TileListEntryOutput>();
  delete = output<TileListEntryOutput>();
  overwrite = output<TileListEntryOutput>();
  protected readonly EntryAction = EntryAction;
}
