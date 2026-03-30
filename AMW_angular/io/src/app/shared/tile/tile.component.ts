import { Component, input, linkedSignal, output } from '@angular/core';
import { NgClass } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { IconComponent } from '../icon/icon.component';
import { ButtonComponent } from '../button/button.component';

@Component({
  selector: 'app-tile-component',
  template: `
    <div class="tile rounded">
      <div
        tabindex="0"
        class="tile-header"
        (keyup.enter)="toggleBody()"
        (click)="toggleBody()"
        [ngClass]="showBody() ? 'opened' : 'closed'"
      >
        <div class="tile-title">
          @if (showBody()) {
            <app-icon icon="caret-down"></app-icon>
          } @else {
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
          @if (noContent()) {
            <div class="no-content">
              <span>No {{ title() }} for this resource</span>
            </div>
          } @else if (notAllowed()) {
            <div class="no-content">
              <span>You are not allowed to view {{ title() }} for this resource</span>
            </div>
          } @else {
            <ng-content></ng-content>
          }
        </div>
      }
    </div>
  `,
  styleUrls: ['./tile.component.scss'],
  providers: [],
  standalone: true,
  imports: [FormsModule, NgClass, IconComponent, ButtonComponent],
})
export class TileComponent {
  title = input.required<string>();
  actionName = input.required<string>();
  canAction = input<boolean>(false);
  isVisible = input<boolean>(false);

  noContent = input<boolean>(false);
  notAllowed = input<boolean>(false);

  tileAction = output<void>();

  showBody = linkedSignal(() => this.isVisible());

  toggleBody() {
    this.showBody.update((current) => !current);
  }

  doTileAction(event: PointerEvent) {
    event.preventDefault();
    event.stopPropagation();
    this.tileAction.emit();
  }
}
