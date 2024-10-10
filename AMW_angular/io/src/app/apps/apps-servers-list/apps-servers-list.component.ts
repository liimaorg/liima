import { Component, Input } from '@angular/core';
import { AsyncPipe } from '@angular/common';
import { PaginationComponent } from '../../shared/pagination/pagination.component';
import { AppServer } from '../app-server';
import { AppsListComponent } from '../apps-list/apps-list-component';

@Component({
  selector: 'app-apps-servers-list',
  standalone: true,
  imports: [AsyncPipe, AppsListComponent, PaginationComponent],
  templateUrl: './apps-servers-list.component.html',
  styleUrl: './apps-servers-list.component.scss',
})
export class AppsServersListComponent {
  @Input() appServers: AppServer[];
}
