import { Component, input, linkedSignal, output } from '@angular/core';
import { NgClass } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { IconComponent } from '../icon/icon.component';
import { ButtonComponent } from '../button/button.component';

@Component({
  selector: 'app-tile-component',
  template: `
    <section class="tile" [ngClass]="{ rounded: !noBorder(), 'no-border': noBorder() }" role="group">
      <div
        tabindex="0"
        class="tile-header"
        (keyup.enter)="noCollapse() ? null : toggleBody()"
        (click)="noCollapse() ? null : toggleBody()"
        [ngClass]="noCollapse() || showBody() ? 'opened' : 'closed'"
        [attr.role]="noCollapse() ? null : 'button'"
        [attr.aria-expanded]="noCollapse() ? null : showBody()"
      >
        <div class="tile-title" [attr.aria-level]="headerLevel()" role="heading">
          @if (!noCollapse()) {
            @if (showBody()) {
              <app-icon icon="caret-down"></app-icon>
            } @else {
              <app-icon icon="caret-right"></app-icon>
            }
          }
          {{ title() }}
        </div>
        @if (canAction() && (noCollapse() || showBody())) {
          <div class="tile-action-bar">
            <app-button [variant]="'primary'" [size]="'sm'" (click)="doTileAction($event)">
              <app-icon icon="plus-circle" />
              <span>{{ actionName() }}</span></app-button
            >
          </div>
        }
      </div>
      @if (noCollapse() || showBody()) {
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
    </section>
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
  isCollapsed = input<boolean>(true);

  noContent = input<boolean>(false);
  notAllowed = input<boolean>(false);
  noBorder = input<boolean>(false);
  noCollapse = input<boolean>(false);
  headerLevel = input<number>(2);

  tileAction = output<void>();

  showBody = linkedSignal(() => !this.isCollapsed());

  toggleBody() {
    this.showBody.update((current) => !current);
  }

  doTileAction(event: PointerEvent) {
    event.preventDefault();
    event.stopPropagation();
    this.tileAction.emit();
  }
}
