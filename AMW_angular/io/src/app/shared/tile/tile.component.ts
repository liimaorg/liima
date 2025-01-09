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
    <div class="tile rounded">
      <div class="tile-header" (click)="toggleBody()" [ngClass]="showBody() ? 'opened' : 'closed'">
        <div class="tile-title">
          @if (showBody()) {
          <app-icon icon="caret-down"></app-icon>} @else {
          <app-icon icon="caret-right"></app-icon>
          }
          {{ title() }}
        </div>
        @if (canAction() && showBody()) {
        <div class="tile-action-bar">
          <app-button [variant]="'primary'" [size]="'sm'" (click)="doTileAction($event)">
            <app-icon icon="plus-circle" />
            <span>{{ actionName() }}</span></app-button
          >
        </div>
        }
      </div>
      @if (showBody()) {
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
      }
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
  isVisible = input<boolean>(false);
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

  showBody = signal(this.isVisible());
  toggleBody() {
    this.showBody.set(!this.showBody());
  }

  doTileAction(event: any) {
    event.preventDefault();
    event.stopPropagation();
    this.tileAction.emit();
  }
}
