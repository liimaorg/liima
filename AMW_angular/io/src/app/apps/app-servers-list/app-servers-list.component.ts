import { Component, Input, OnInit, Signal } from '@angular/core';
import { AsyncPipe } from '@angular/common';
import { PaginationComponent } from '../../shared/pagination/pagination.component';
import { AppServer } from '../app-server';
import { AppsListComponent } from '../apps-list/apps-list-component';

@Component({
  selector: 'amw-app-servers-list',
  standalone: true,
  imports: [AsyncPipe, AppsListComponent, PaginationComponent],
  templateUrl: './app-servers-list.component.html',
  styleUrl: './app-servers-list.component.scss',
})
export class AppServersListComponent {
  @Input() appServers: AppServer[];
}
