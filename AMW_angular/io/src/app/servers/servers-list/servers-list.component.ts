import { ChangeDetectionStrategy, Component, inject, input, Signal } from '@angular/core';
import { AppsListComponent } from '../../apps/apps-list/apps-list-component';
import { Server } from '../server';

@Component({
  selector: 'app-servers-list',
  standalone: true,
  imports: [AppsListComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: "./servers-list.component.html"
})
export class ServersListComponent {
  servers = input.required<Server[]>();
  canReadAppServer = input.required<boolean>();
  canReadResources = input.required<boolean>();
  linkToHostUrl = input.required<string>();
}
