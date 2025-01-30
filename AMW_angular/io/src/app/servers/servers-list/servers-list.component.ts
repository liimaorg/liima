import { ChangeDetectionStrategy, Component, computed, inject, input, Signal } from '@angular/core';
import { Server } from '../server';
import { TableComponent, TableHeader } from '../../shared/table/table.component';

@Component({
  selector: 'app-servers-list',
  standalone: true,
  imports: [TableComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './servers-list.component.html',
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
          hostLinkUrl: this.linkToHostUrl() + '=' + server.host,
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

  serversHeader(): TableHeader<{
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
        title: 'Host',
        type: 'link',
        urlKey: 'hostLinkUrl',
      },
      {
        key: 'environment',
        title: 'Env',
      },
      {
        key: 'appServer',
        title: 'AppServer',
        type: 'link',
        urlKey: 'appServerLinkUrl',
      },
      {
        key: 'appServerRelease',
        title: 'AppServer Release',
      },
      {
        key: 'runtime',
        title: 'Runtime',
      },
      {
        key: 'node',
        title: 'Node',
        type: 'link',
        urlKey: 'nodeLinkUrl',
      },
      {
        key: 'nodeRelease',
        title: 'Node Release',
      },
    ];
  }
}
