import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { AppsListComponent } from '../../apps/apps-list/apps-list-component';
import { Server } from '../server';

@Component({
  selector: 'app-servers-list',
  standalone: true,
  imports: [AppsListComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `<div class="table-responsive">
    @if (servers() && servers().length > 0) {
    <table class="table table-sm table-borderless">
      <thead class="table-light">
        <tr>
          <th>Host</th>
          <th>Env</th>
          <th>AppServer</th>
          <th>AppServer Release</th>
          <th>Runtime</th>
          <th>Node</th>
          <th>Node Release</th>
        </tr>
      </thead>
      <tbody>
        @for (server of servers(); track server; let even = $even) {
        <tr [class.table-light]="!even">
          <td>{{ server.host }}</td>
          <td>{{ server.environment }}</td>
          <td>{{ server.appServer }}</td>
          <td>{{ server.appServerRelease }}</td>
          <td>{{ server.runtime }}</td>
          <td>{{ server.node }}</td>
          <td>{{ server.nodeRelease }}</td>
        </tr>
        }
      </tbody>
    </table>
    }
  </div>`,
})
export class ServersListComponent {
  servers = input.required<Server[]>();
}
