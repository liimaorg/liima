import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { PageComponent } from '../layout/page/page.component';
import { LoadingIndicatorComponent } from '../shared/elements/loading-indicator.component';
import { AuthService } from '../auth/auth.service';
import { ServersListComponent } from './servers-list/servers-list.component';
import { ServersService } from './servers.service';

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
  private serversService = inject(ServersService);

  isLoading = signal(false);

  servers = this.serversService.servers;

  permissions = computed(() => {
    if (this.authService.restrictions().length > 0) {
      return {
        canViewSomething: true,
      };
    } else {
      return { canViewSomething: false };
    }
  });
}
