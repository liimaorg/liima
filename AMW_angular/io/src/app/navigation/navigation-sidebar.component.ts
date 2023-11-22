import { Component, Input, Output, EventEmitter } from '@angular/core';
import { NavigationItem } from './navigation-item';
import { Navigation } from './navigation-store.service';
import { NgIf, NgFor, NgClass } from '@angular/common';

@Component({
    selector: 'app-navigation-sidebar',
    template: `
    <ul
      *ngIf="navigation.visible"
      class="nav flex-column pt-4 d-none d-sm-block"
    >
      <ng-container *ngIf="!navigation.items">
        <li class="nav-item active">
          <a class="nav-link" href="#"
            >{{ navigation.current }}<span class="visually-hidden">(current)</span></a
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
    standalone: true,
    imports: [
        NgIf,
        NgFor,
        NgClass,
    ],
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
