import { ServersListComponent } from './servers-list.component';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ComponentRef } from '@angular/core';
import { Server } from '../server';

describe(ServersListComponent.name, () => {
  let component: ServersListComponent;
  let componentRef: ComponentRef<ServersListComponent>;
  let fixture: ComponentFixture<ServersListComponent>;

  const servers: Server[] = [
    {
      host: 'host1',
      appServer: 'Application Server 1',
      appServerRelease: 'multiple',
      runtime: 'multiple runtimes',
      node: 'node1',
      nodeRelease: '1.1',
      environment: 'A',
      appServerId: 1,
      nodeId: 2,
      environmentId: 3,
      domain: 'domain 1',
      domainId: '1',
      definedOnNode: true,
    },
    {
      host: 'host2',
      appServer: 'Application Server 2',
      appServerRelease: 'multiple',
      runtime: 'multiple runtimes',
      node: 'node2',
      nodeRelease: '1.2',
      environment: 'B',
      appServerId: 2,
      nodeId: 3,
      environmentId: 4,
      domain: 'domain 2',
      domainId: '2',
      definedOnNode: false,
    },
  ];

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ServersListComponent],
      providers: [],
    }).compileComponents();

    fixture = TestBed.createComponent(ServersListComponent);

    component = fixture.componentInstance;
    componentRef = fixture.componentRef;
    componentRef.setInput('servers', servers);
    componentRef.setInput('canReadAppServer', true);
    componentRef.setInput('canReadResources', false);
    componentRef.setInput('linkToHostUrl', '/link/to/host');
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeDefined();
    expect(component.servers().length).toBe(2);
    expect(component.canReadResources()).toBeFalse();
    expect(component.canReadAppServer()).toBeTrue();
    expect(component.linkToHostUrl()).toEqual('/link/to/host');
  });
});
