import { Component, Input, Output, EventEmitter } from '@angular/core';
import { NavigationItem } from './navigation-item';
import { Navigation } from './navigation-store.service';

@Component({
  selector: 'app-navigation-sidebar',
  template: `
    <ul
      *ngIf="navigation.visible"
      class="nav flex-column pt-4 d-none d-sm-block pt-5"
    >
      <ng-container *ngIf="!navigation.items">
        <li class="nav-item active">
          <a class="nav-link" href="#"
            >{{ navigation.current }}<span class="sr-only">(current)</span></a
          >
        </li>
      </ng-container>
      <ng-container *ngIf="navigation.items">
        <ng-container *ngFor="let item of navigation.items">
          <li
            class="nav-item"
            [ngClass]="item.title === navigation.current ? 'active' : ''"
          >
            <a
              class="nav-link"
              href="#{{ item.target }}"
              (mouseup)="itemSelected.emit(item)"
              >{{ item.title }}</a
            >
          </li>
        </ng-container>
      </ng-container>
    </ul>
  `,
  styleUrls: ['./navigation-sidebar.component.scss'],
})
export class NavigationSidebarComponent {
  @Input()
  navigation: Navigation;

  @Output()
  itemSelected: EventEmitter<NavigationItem> = new EventEmitter<
    NavigationItem
  >();

  constructor() {}
}
