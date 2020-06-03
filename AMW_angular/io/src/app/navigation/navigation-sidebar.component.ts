import { Component, Input, Output, EventEmitter } from '@angular/core';
import { NavigationItem } from './navigation-item';
import { Navigation } from './navigation-store.service';

@Component({
  selector: 'app-navigation-sidebar',
  template: `
    <div *ngIf="navigation.visible" class="col-sm-2 col-lg-1 sidebar">
      <ul class="nav nav-sidebar">
        <ng-container *ngIf="!navigation.items">
          <li class="active">
            <a href="#"
              >{{ navigation.current }}<span class="sr-only">(current)</span></a
            >
          </li>
        </ng-container>
        <ng-container *ngIf="navigation.items">
          <ng-container *ngFor="let item of navigation.items">
            <li [ngClass]="item.title === navigation.current ? 'active' : ''">
              <a
                href="#{{ item.target }}"
                (mouseup)="itemSelected.emit(item)"
                >{{ item.title }}</a
              >
            </li>
          </ng-container>
        </ng-container>
      </ul>
    </div>
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
