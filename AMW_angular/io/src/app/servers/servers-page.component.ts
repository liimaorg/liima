import { ChangeDetectionStrategy, Component, computed, inject, Signal, signal } from '@angular/core';
import { PageComponent } from '../layout/page/page.component';
import { LoadingIndicatorComponent } from '../shared/elements/loading-indicator.component';
import { AuthService } from '../auth/auth.service';
import { ServersListComponent } from './servers-list/servers-list.component';
import { ServersService } from './servers.service';
import { ConfigurationService } from '../shared/service/configuration.service';
import { ENVIRONMENT } from '../core/amw-constants';
import { Config, pluck } from '../shared/configuration';

@Component({
  selector: 'app-servers-page',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [PageComponent, LoadingIndicatorComponent, ServersListComponent],
  template: ` <app-loading-indicator [isLoading]="isLoading()"></app-loading-indicator>
    <app-page>
      <div class="page-title">Servers</div>
      <div class="page-content">
        <app-servers-list
          [servers]="servers()"
          [canReadAppServer]="permissions().canReadAppServer"
          [canReadResources]="permissions().canReadResources"
          [linkToHostUrl]="linkToHostUrl()"
        /></div
    ></app-page>`,
})
export class ServersPageComponent {
  private authService = inject(AuthService);
  private serversService = inject(ServersService);
  private configurationService = inject(ConfigurationService);

  isLoading = signal(false);

  servers = this.serversService.servers;
  configuration: Signal<Config[]> = this.configurationService.configuration;

  linkToHostUrl = computed(() => {
    if (!this.configuration()) return;
    const config = this.configuration();
    const vmDetailUrl = pluck(ENVIRONMENT.AMW_VM_DETAILS_URL, config);
    const vmUrlParam = pluck(ENVIRONMENT.AMW_VM_URL_PARAM, config);
    return `${vmDetailUrl}?${vmUrlParam}=`;
  });

  permissions = computed(() => {
    if (this.authService.restrictions().length > 0) {
      return {
        canReadAppServer: this.authService.hasResourcePermission('RESOURCE', 'CREATE', 'APPLICATION'),
        canReadResources: this.authService.hasPermission('RESOURCE', 'READ'),
      };
    } else {
      return {
        canReadAppServer: false,
        canReadResources: false,
      };
    }
  });
}
