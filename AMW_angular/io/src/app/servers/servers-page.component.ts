import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { PageComponent } from '../layout/page/page.component';
import { LoadingIndicatorComponent } from '../shared/elements/loading-indicator.component';
import { AuthService } from '../auth/auth.service';
import { Server, ServersListComponent } from './servers-list/servers-list.component';

@Component({
  selector: 'app-servers-page',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [PageComponent, LoadingIndicatorComponent, ServersListComponent],
  template: ` <app-loading-indicator [isLoading]="isLoading()"></app-loading-indicator>
    <app-page>
      <div class="page-title">Servers</div>
      <div class="page-content">
        {{ permissions() }}
        <app-servers-list [servers]="servers()" /></div
    ></app-page>`,
})
export class ServersPageComponent {
  private authService = inject(AuthService);

  isLoading = signal(false);

  permissions = computed(() => {
    if (this.authService.restrictions().length > 0) {
      return {
        canViewSomething: true,
      };
    } else {
      return { canViewSomething: false };
    }
  });

  servers = signal<Server[]>([
    {
      host: 'host1',
      environment: 'A',
      appServer: 'Application Server 1',
      appServerRelease: 'multiple',
      runtime: 'multiple runtimes',
      node: 'node1',
      nodeRelease: '1.1',
    },
    {
      host: 'host2',
      environment: 'B',
      appServer: 'Application Server 2',
      appServerRelease: 'multiple',
      runtime: 'multiple runtimes',
      node: 'node2',
      nodeRelease: '1.2',
    },
  ]);

}
