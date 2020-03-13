import { Component, Input, Output, EventEmitter } from '@angular/core';
import { NavigationItem } from './navigation-item';

@Component({
  selector: 'app-navigation-sidebar',
  template: `
    <div *ngIf="navigationState.isVisible" class="col-sm-2 col-lg-1 sidebar">
      <ul class="nav nav-sidebar">
        <ng-container *ngIf="!navigationState.navigationItems">
          <li class="active">
            <a href="#"
              >{{ navigationState.current
              }}<span class="sr-only">(current)</span></a
            >
          </li>
        </ng-container>
        <ng-container *ngIf="navigationState.navigationItems">
          <ng-container *ngFor="let item of navigationState.navigationItems">
            <li
              [ngClass]="item.title === navigationState.current ? 'active' : ''"
            >
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
  styleUrls: ['./navigation-sidebar.component.scss']
})
export class NavigationSidebarComponent {
  @Input()
  navigationState: any;

  @Output()
  itemSelected: EventEmitter<NavigationItem> = new EventEmitter<
    NavigationItem
  >();

  constructor() {}
}
