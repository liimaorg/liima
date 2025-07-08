import { ChangeDetectionStrategy, Component, computed, input } from '@angular/core';
import { Server } from '../server';
import { TableComponent, TableColumnType } from '../../shared/table/table.component';

@Component({
    selector: 'app-servers-list',
    imports: [TableComponent],
    changeDetection: ChangeDetectionStrategy.OnPush,
    templateUrl: './servers-list.component.html'
})
export class ServersListComponent {
  servers = input.required<Server[]>();
  canReadAppServer = input.required<boolean>();
  canReadResources = input.required<boolean>();
  linkToHostUrl = input.required<string>();

  serversTableData = computed(
    () =>
      this.servers()?.map((server) => {
        return {
          host: server.host,
          hostLinkUrl: this.linkToHostUrl() ? this.linkToHostUrl().replace('{hostName}', server.host) : undefined,
          environment: server.environment,
          appServer: server.appServer,
          appServerLinkUrl: this.canReadAppServer()
            ? '/AMW_web/pages/editResourceView.xhtml?ctx=' + server.environmentId + '&id=' + server.appServerId
            : null,
          appServerRelease: server.appServerRelease,
          runtime: server.runtime,
          node: server.node,
          nodeLinkUrl: this.canReadResources()
            ? '/AMW_web/pages/editResourceView.xhtml?ctx=' + server.environmentId + '&id=' + server.nodeId
            : null,
          nodeRelease: server.nodeRelease,
        };
      }),
  );

  serversHeader(): TableColumnType<{
    host: string;
    hostLinkUrl: string;
    appServer: string;
    appServerLinkUrl: string;
    appServerRelease: string;
    runtime: string;
    node: string;
    nodeLinkUrl: string;
    nodeRelease: string;
    environment: string;
  }>[] {
    return [
      {
        key: 'host',
        columnTitle: 'Host',
        cellType: 'link',
        urlKey: 'hostLinkUrl',
      },
      {
        key: 'environment',
        columnTitle: 'Env',
      },
      {
        key: 'appServer',
        columnTitle: 'AppServer',
        cellType: 'link',
        urlKey: 'appServerLinkUrl',
      },
      {
        key: 'appServerRelease',
        columnTitle: 'AppServer Release',
      },
      {
        key: 'runtime',
        columnTitle: 'Runtime',
      },
      {
        key: 'node',
        columnTitle: 'Node',
        cellType: 'link',
        urlKey: 'nodeLinkUrl',
      },
      {
        key: 'nodeRelease',
        columnTitle: 'Node Release',
      },
    ];
  }
}
