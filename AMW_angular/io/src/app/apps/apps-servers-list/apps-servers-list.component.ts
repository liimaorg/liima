import { Component, input } from '@angular/core';
import { AppServer } from '../app-server';
import { AppsListComponent } from '../apps-list/apps-list-component';

@Component({
  selector: 'app-apps-servers-list',
  imports: [AppsListComponent],
  templateUrl: './apps-servers-list.component.html',
  styleUrl: './apps-servers-list.component.scss',
})
export class AppsServersListComponent {
  appServers = input.required<AppServer[]>();
}
