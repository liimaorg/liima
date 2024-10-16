import { Server, ServersListComponent } from './servers-list.component';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ComponentRef } from '@angular/core';

describe(ServersListComponent.name, () => {
  let component: ServersListComponent;
  let componentRef: ComponentRef<ServersListComponent>;
  let fixture: ComponentFixture<ServersListComponent>;

  const servers: Server[] = [
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
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeDefined();
    expect(component.servers().length).toBe(2);
  });
});
