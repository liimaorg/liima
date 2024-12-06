import { ChangeDetectionStrategy, Component, computed, inject, Signal } from '@angular/core';
import { PageComponent } from '../layout/page/page.component';
import { LoadingIndicatorComponent } from '../shared/elements/loading-indicator.component';
import { AuthService } from '../auth/auth.service';
import { ServersListComponent } from './servers-list/servers-list.component';
import { ServersService } from './servers.service';
import { ConfigurationService } from '../shared/service/configuration.service';
import { ENVIRONMENT } from '../core/amw-constants';
import { Config, pluck } from '../shared/configuration';
import { ServersFilterComponent } from './servers-filter/servers-filter.component';
import { EnvironmentService } from '../deployment/environment.service';
import { ServerFilter } from './servers-filter/server-filter';

@Component({
  selector: 'app-servers-page',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [PageComponent, LoadingIndicatorComponent, ServersListComponent, ServersFilterComponent],
  template: ` <app-loading-indicator [isLoading]="isLoading"></app-loading-indicator>
    <app-page>
      <div class="page-title">Servers</div>
      <div class="page-content">
        <div class="container">
          <app-servers-filter
            [environments]="environments()"
            [runtimes]="runtimes()"
            [appServerSuggestions]="appServerSuggestions()"
            (searchFilter)="searchFilter($event)"
          />
          <app-servers-list
            [servers]="servers()"
            [canReadAppServer]="permissions().canReadAppServer"
            [canReadResources]="permissions().canReadResources"
            [linkToHostUrl]="linkToHostUrl()"
          />
        </div>
      </div>
    </app-page>`,
})
export class ServersPageComponent {
  private authService = inject(AuthService);
  private serversService = inject(ServersService);
  private environmentsService = inject(EnvironmentService);
  private configurationService = inject(ConfigurationService);

  isLoading: boolean = false;

  environments = this.environmentsService.envs;
  runtimes = this.serversService.runtimes;
  appServerSuggestions = this.serversService.appServersSuggestions;
  servers = computed(() => {
    this.isLoading = false;
    return this.serversService.servers();
  });
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

  searchFilter($event: ServerFilter) {
    this.isLoading = true;
    this.serversService.setServerFilter($event);
  }
}
