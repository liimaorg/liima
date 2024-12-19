import { Component, input, output, signal } from '@angular/core';
import { NgClass, NgComponentOutlet } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { NgbDatepicker, NgbPopover, NgbTimepicker } from '@ng-bootstrap/ng-bootstrap';
import { IconComponent } from '../icon/icon.component';
import { ButtonComponent } from '../button/button.component';
import { TileListComponent, TileListEntry, TileListEntryOutput } from './tile-list/tile-list.component';

@Component({
  selector: 'app-tile-component',
  template: `
    <div class="tile">
      <div class="tile-header">
        <div class="tile-title">{{ title() }}</div>
        @if (canAction()) {
        <div class="tile-action-bar">
          <app-button [variant]="'primary'" [size]="'sm'" (click)="tileAction.emit()">
            <app-icon icon="plus-circle" />
            <span>{{ actionName() }}</span></app-button
          >
        </div>
        }
      </div>

      <div class="tile-body">
        @if (!lists()) {
        <div class="no-content">
          <span>No {{ title() }} for this resource</span>
        </div>
        } @else if (lists().length <= 0) {
        <div class="no-content">
          <span>You are not allowed to view {{ title() }} for this resource</span>
        </div>
        }
        <ng-container #container></ng-container>
        @for (list of lists(); track list) {
        <app-tile-list
          [title]="list.title"
          [data]="list.entries"
          [canEdit]="list.canEdit"
          [canDelete]="list.canDelete"
          [canOverwrite]="list.canOverwrite"
          (edit)="listAction.emit($event)"
          (delete)="listAction.emit($event)"
          (overwrite)="listAction.emit($event)"
        ></app-tile-list>
        }
      </div>
    </div>
  `,
  styleUrls: ['./tile.component.scss'],
  providers: [],
  standalone: true,
  imports: [
    FormsModule,
    NgClass,
    NgbPopover,
    NgComponentOutlet,
    IconComponent,
    NgbDatepicker,
    NgbTimepicker,
    ButtonComponent,
    TileListComponent,
  ],
})
export class TileComponent {
  title = input.required<string>();
  actionName = input.required<string>();
  canAction = input<boolean>(false);
  lists = input.required<
    {
      title: string;
      entries: TileListEntry[];
      canEdit?: boolean;
      canDelete?: boolean;
      canOverwrite?: boolean;
    }[]
  >();
  tileAction = output<void>();
  listAction = output<TileListEntryOutput>();
}
