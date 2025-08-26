import { ChangeDetectionStrategy, Component, computed, inject, Signal, OnInit } from '@angular/core';
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
import { isServerFilterEmpty, ServerFilter } from './servers-filter/server-filter';
import { ActivatedRoute, Router } from '@angular/router';

@Component({
  selector: 'app-servers-page',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [PageComponent, LoadingIndicatorComponent, ServersListComponent, ServersFilterComponent],
  template: `<app-loading-indicator [isLoading]="isLoading"></app-loading-indicator>
    <app-page>
      <div class="page-title">Servers</div>
      <div class="page-content">
        <div class="container">
          <div class="card row mt-1 mb-1">
            <div class="card-header">
              <app-servers-filter
                [environments]="environments()"
                [runtimes]="runtimes()"
                [appServerSuggestions]="appServerSuggestions()"
                [inputSearchFilter]="serversService.serverFilter()"
                (searchFilter)="searchFilter($event)"
              />
            </div>
            <div class="card-body">
              <app-servers-list
                [servers]="servers()"
                [canReadAppServer]="permissions().canReadAppServer"
                [canReadResources]="permissions().canReadResources"
                [linkToHostUrl]="linkToHostUrl()"
              />
            </div>
          </div>
        </div>
      </div>
    </app-page>`,
})
export class ServersPageComponent implements OnInit {
  private authService = inject(AuthService);
  public serversService = inject(ServersService);
  private environmentsService = inject(EnvironmentService);
  private configurationService = inject(ConfigurationService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);

  isLoading: boolean = false;
  hasSearched = false;

  environments = this.environmentsService.envs;
  runtimes = this.serversService.runtimes;
  appServerSuggestions = this.serversService.appServersSuggestions;
  servers = computed(() => {
    this.isLoading = false;
    return this.serversService.servers();
  });
  configuration: Signal<Config[]> = this.configurationService.configuration;

  ngOnInit() {
    this.route.queryParams.subscribe((params: ServerFilter) => {
      if (this.hasSearched || !isServerFilterEmpty(params)) {
        this.serversService.setServerFilter(params);
      }
    });
  }

  linkToHostUrl = computed(() => {
    if (!this.configuration()) return;
    const config = this.configuration();
    const vmDetailUrl = pluck(ENVIRONMENT.AMW_VM_DETAILS_URL, config);
    const vmUrlParam = pluck(ENVIRONMENT.AMW_VM_URL_PARAM, config);
    if (vmUrlParam) {
      // old logic for backward compatibility
      return `${vmDetailUrl}?${vmUrlParam}={hostName}`;
    }
    return vmDetailUrl; // {hostName} will be replaced with the actual host name
  });

  permissions = computed(() => {
    if (this.authService.restrictions().length > 0) {
      return {
        canReadAppServer: this.authService.hasPermission('RESOURCE', 'CREATE', 'APPLICATION'),
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
    this.hasSearched = true;
    this.isLoading = true;
    this.router.navigate(['/servers'], { queryParams: $event });
    this.serversService.setServerFilter($event);
  }
}
