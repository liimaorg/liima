import { provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { of, Subject } from 'rxjs';
import { Release } from '../resources/models/release';
import { Resource } from '../resources/models/resource';
import { ResourceTag } from '../resources/models/resource-tag';
import { ResourceService } from '../resources/services/resource.service';
import { AppWithVersion } from './app-with-version';
import { Deployment } from './deployment';
import { DeploymentParameter } from './deployment-parameter';
import { DeploymentRequest } from './deployment-request';
import { DeploymentComponent } from './deployment.component';
import { DeploymentService } from './deployment.service';
import { Environment } from './environment';
import { EnvironmentService } from './environment.service';
import { DateTimeModel } from '../shared/date-time-picker/date-time.model';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { Component, signal } from '@angular/core';

@Component({
  template: '',
  imports: [FormsModule],
  providers: [provideHttpClientTesting()],
})
class DummyComponent {}

describe('DeploymentComponent (create deployment)', () => {
  let component: DeploymentComponent;
  let fixture: ComponentFixture<DeploymentComponent>;
  let resourceService: ResourceService;
  let environmentService: EnvironmentService;
  let deploymentService: DeploymentService;

  const mockRoute: any = { snapshot: {} };

  mockRoute.params = new Subject<any>();
  mockRoute.queryParams = new Subject<any>();

  beforeEach(() => {
    TestBed.configureTestingModule({
      teardown: { destroyAfterEach: false },
      imports: [FormsModule, DeploymentComponent, DummyComponent],
      providers: [
        ResourceService,
        EnvironmentService,
        DeploymentService,
        { provide: ActivatedRoute, useValue: mockRoute },
        provideHttpClient(withInterceptorsFromDi()),
        provideHttpClientTesting(),
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(DeploymentComponent);
    component = fixture.componentInstance;

    resourceService = TestBed.inject(ResourceService);
    environmentService = TestBed.inject(EnvironmentService);
    deploymentService = TestBed.inject(DeploymentService);
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should have default data', () => {
    expect(component.appserverName).toEqual('');
    expect(component.releaseName).toEqual('');
    expect(component.deploymentId).toBeUndefined();
    expect(component.isLoading()).toBeFalsy();
    expect(component.isRedeployment()).toBeFalsy();
  });

  it('should not be ready for deployment', () => {
    expect(component.isReadyForDeployment()).toBeFalsy();
    component.ngOnInit();
    expect(component.isReadyForDeployment()).toBeFalsy();
  });

  it('should call environmentService on ngOnInit', async () => {
    vi.spyOn(environmentService, 'getAll').mockReturnValue(of([]));
    expect(environmentService.getAll).not.toHaveBeenCalled();
    fixture.detectChanges();
    component.ngOnInit();
    mockRoute.params.next({});
    await fixture.whenStable();
    expect(environmentService.getAll).toHaveBeenCalled();
  });

  it('should populate groupedEnvironments on ngOnInit', async () => {
    const environments: Environment[] = [{ id: 1, name: 'A', parentName: 'DEV' } as Environment];
    vi.spyOn(environmentService, 'getAll').mockReturnValue(of(environments));
    expect(component.groupedEnvironments()).toEqual({});
    fixture.detectChanges();
    component.ngOnInit();
    mockRoute.params.next({});
    await fixture.whenStable();
    expect(component.groupedEnvironments()['DEV'].length).toBe(1);
  });

  it('should call deploymentService on ngAfterViewInit', () => {
    // given
    vi.spyOn(deploymentService, 'getAllDeploymentParameterKeys').mockReturnValue(of([]));
    expect(deploymentService.getAllDeploymentParameterKeys).not.toHaveBeenCalled();
    // when
    component.ngAfterViewInit();
    // then
    expect(deploymentService.getAllDeploymentParameterKeys).toHaveBeenCalled();
  });

  it('should set selectedAppserver and selectedRelease', () => {
    // given
    const testReleases: Release[] = [
      { id: 1, release: 'testRelease' } as Release,
      { id: 2, release: 'anotherRelease' } as Release,
    ];
    component.appserverName = 'testServer';
    component.releaseName = 'testRelease';
    component.appservers = signal([{ name: 'testServer', releases: testReleases } as Resource]);
    vi.spyOn(resourceService, 'getDeployableReleases').mockReturnValue(of(testReleases));
    // when
    component.initAppservers();
    // then
    expect(component.selectedAppserver().name).toBe('testServer');
    expect(component.selectedRelease().release).toBe('testRelease');
  });

  it('should set selectedAppserver but not set selectedRelease if not available for selectedAppserver', () => {
    const testReleases: Release[] = [{ id: 1, release: 'testRelease' } as Release];
    component.appserverName = 'testServer';
    component.releaseName = 'missingRelease';
    component.appservers = signal([{ name: 'testServer', releases: testReleases } as Resource]);
    // when
    component.initAppservers();
    // then
    expect(component.selectedAppserver().name).toBe('testServer');
    expect(component.selectedRelease()).toBeNull();
  });

  it('should return environementGroupNames on getEnvironmentGroups()', async () => {
    const environments: Environment[] = [
      { id: 1, name: 'A', parentName: 'DEV' } as Environment,
      { id: 2, name: 'B', parentName: 'DEV' } as Environment,
      { id: 3, name: 'P', parentName: 'PROD' } as Environment,
    ];
    vi.spyOn(environmentService, 'getAll').mockReturnValue(of(environments));
    fixture.detectChanges();
    component.ngOnInit();
    mockRoute.params.next({});
    await fixture.whenStable();

    const groups: string[] = component.getEnvironmentGroups();

    expect(groups.length).toBe(2);
    expect(groups, 'PROD').toContain('DEV');
  });

  it('should keep environments selected on onChangeAppserver', () => {
    // given
    component.selectedRelease.set({ id: 1 } as Release);
    const appServer: Resource = { name: 'testServer' } as Resource;
    component.environments.set([{ id: 1 } as Environment, { id: 2, selected: true } as Environment]);
    component.selectedAppserver.set(appServer);
    // when
    component.onChangeAppserver();
    // then
    expect(component.selectedRelease()).toBeNull();
    expect(component.environments()[0].selected).toBeFalsy();
    expect(component.environments()[1].selected).toBeTruthy();
  });

  it('should check permission on onChangeAppserver', () => {
    // given
    component.selectedRelease.set({ id: 1 } as Release);
    const appServer: Resource = { name: 'testServer', id: 3 } as Resource;
    component.environments.set([{ id: 1 } as Environment, { id: 2, selected: true } as Environment]);
    component.selectedAppserver.set(appServer);
    vi.spyOn(deploymentService, 'canDeploy').mockReturnValue(of(true));
    // when
    component.onChangeAppserver();
    // then
    expect(deploymentService.canDeploy).toHaveBeenCalledWith(3, [2]);
    expect(component.hasPermissionToDeploy()).toBeTruthy();
  });

  it('should call resourceService on onChangeRelease', () => {
    // given
    const testRelease: Release = { id: 1, release: 'testRelease' } as Release;
    const betterRelease: Release = {
      id: 1,
      release: 'betterRelease',
    } as Release;
    component.releases.set([testRelease]);
    component.selectedRelease.set(testRelease);
    vi.spyOn(resourceService, 'getLatestForRelease').mockReturnValue(of(betterRelease));
    vi.spyOn(resourceService, 'getAppsWithVersions').mockReturnValue(of([]));
    component.selectedAppserver.set({
      name: 'testServer',
      releases: [testRelease],
    } as Resource);
    // when
    component.onChangeRelease();
    // then
    expect(resourceService.getLatestForRelease).toHaveBeenCalled();
    expect(resourceService.getAppsWithVersions).toHaveBeenCalled();
  });

  it('should replace deploymentParameters with same key onAddParam', () => {
    // given
    component.transDeploymentParameters = [{ key: 'atest', value: 'foo' } as DeploymentParameter];
    component.transDeploymentParameter = {
      key: 'atest',
      value: 'bar',
    } as DeploymentParameter;
    // when
    component.onAddParam();
    // then
    expect(component.transDeploymentParameters.length).toBe(1);
    expect(component.transDeploymentParameters[0].value).toEqual('bar');
  });

  it('should remove the right deploymentParameter onRemoveParam', () => {
    // given
    const parameterA: DeploymentParameter = {
      key: 'atest',
      value: 'foo',
    } as DeploymentParameter;
    const parameterB: DeploymentParameter = {
      key: 'btest',
      value: 'bar',
    } as DeploymentParameter;
    component.transDeploymentParameters = [parameterA, parameterB];
    // when
    component.onRemoveParam(parameterA);
    // then
    expect(component.transDeploymentParameters.length).toBe(1);
    expect(component.transDeploymentParameters[0].value).toEqual(parameterB.value);
  });

  it('should not be readyForDeployment if no environment is selected', () => {
    // given
    component.selectedAppserver.set({ name: 'testServer' } as Resource);
    component.selectedRelease.set({ id: 1, release: 'testRelease' } as Release);
    component.environments.set([{ id: 1 } as Environment]);
    // when then
    expect(component.isReadyForDeployment()).toBeFalsy();
  });

  it('should not be readyForDeployment if a release ist set, an environment is selected but appWithVersions is empty', () => {
    // given
    component.selectedAppserver.set({ name: 'testServer' } as Resource);
    component.selectedRelease.set({ id: 1, release: 'testRelease' } as Release);
    component.environments.set([{ id: 1 } as Environment, { id: 2, selected: true } as Environment]);
    // when then
    expect(component.isReadyForDeployment()).toBeFalsy();
  });

  it('should be readyForDeployment if a release ist set, an environment is selected and appWithVersions is not empty', () => {
    // given
    component.selectedAppserver.set({ name: 'testServer' } as Resource);
    component.selectedRelease.set({ id: 1, release: 'testRelease' } as Release);
    component.environments.set([{ id: 1 } as Environment, { id: 2, selected: true } as Environment]);
    component.appsWithVersion.set([{ applicationName: 'testApp' } as AppWithVersion]);
    // when then
    expect(component.isReadyForDeployment()).toBeTruthy();
  });

  it('should call the deploymentService with the right values on requestDeployment', () => {
    // given
    component.selectedAppserver.set({ name: 'testServer' } as Resource);
    component.selectedRelease.set({ id: 1, release: 'testRelease' } as Release);
    component.environments.set([
      { id: 2, name: 'A' } as Environment,
      { id: 3, name: 'B', selected: true } as Environment,
    ]);
    component.appsWithVersion.set([
      {
        applicationId: 4,
        applicationName: 'testApp',
        version: '1.2.3',
      } as AppWithVersion,
    ]);
    component.selectedResourceTag.set({
      id: 5,
      tagDate: 1485378084103,
    } as ResourceTag);
    component.deploymentDate = DateTimeModel.fromLocalString('02.01.2017 12:00');
    component.transDeploymentParameters = [
      { key: 'atest', value: 'foo' } as DeploymentParameter,
      { key: 'btest', value: 'bar' } as DeploymentParameter,
    ];
    const deploymentRequest: DeploymentRequest = {
      appServerName: 'testServer',
      contextIds: [3],
      releaseName: 'testRelease',
      simulate: false,
      sendEmail: false,
      requestOnly: true,
      appsWithVersion: component.appsWithVersion(),
      stateToDeploy: component.selectedResourceTag().tagDate,
      deploymentDate: DateTimeModel.fromLocalString('02.01.2017 12:00').toEpoch(),
      deploymentParameters: component.transDeploymentParameters,
    } as DeploymentRequest;
    vi.spyOn(deploymentService, 'createDeployment').mockReturnValue(of({ trackingId: 910 } as Deployment));
    // when
    component.requestDeployment();
    // then
    expect(deploymentService.createDeployment).toHaveBeenCalledWith(deploymentRequest);
    expect(component.successMessage()).toContain('Tracking Id 910');
  });

  it('should call the deploymentService with the right values on createDeployment', () => {
    // given
    component.selectedAppserver.set({ name: 'testServer' } as Resource);
    component.selectedRelease.set({ id: 1, release: 'testRelease' } as Release);
    component.environments.set([
      { id: 2, name: 'A', selected: true } as Environment,
      { id: 3, name: 'B', selected: true } as Environment,
    ]);
    component.simulate = true;
    component.appsWithVersion.set([
      {
        applicationId: 4,
        applicationName: 'testApp',
        version: '1.2.3',
      } as AppWithVersion,
      {
        applicationId: 5,
        applicationName: 'testAPP',
        version: '1.2.3.4',
      } as AppWithVersion,
    ]);
    component.selectedResourceTag.set({
      id: 5,
      tagDate: 1485378084103,
    } as ResourceTag);
    component.transDeploymentParameters = [
      { key: 'atest', value: 'foo' } as DeploymentParameter,
      { key: 'btest', value: 'bar' } as DeploymentParameter,
    ];
    const deploymentRequest: DeploymentRequest = {
      appServerName: 'testServer',
      contextIds: [2, 3],
      releaseName: 'testRelease',
      simulate: component.simulate,
      sendEmail: false,
      requestOnly: false,
      appsWithVersion: component.appsWithVersion(),
      stateToDeploy: component.selectedResourceTag().tagDate,
      deploymentParameters: component.transDeploymentParameters,
    } as DeploymentRequest;
    vi.spyOn(deploymentService, 'createDeployment').mockReturnValue(of({ trackingId: 911 } as Deployment));
    // when
    component.createDeployment();
    // then
    expect(deploymentService.createDeployment).toHaveBeenCalledWith(deploymentRequest);
    expect(component.successMessage()).toContain('Tracking Id 911');
  });
});

describe('DeploymentComponent (create deployment with params)', () => {
  let component: DeploymentComponent;
  let fixture: ComponentFixture<DeploymentComponent>;
  const mockRoute: any = { snapshot: {} };

  mockRoute.params = new Subject<any>();
  mockRoute.queryParams = new Subject<any>();

  beforeEach(() => {
    TestBed.configureTestingModule({
      teardown: { destroyAfterEach: false },
      imports: [FormsModule, DeploymentComponent, DummyComponent],
      providers: [
        ResourceService,
        EnvironmentService,
        DeploymentService,
        { provide: ActivatedRoute, useValue: mockRoute },
        provideHttpClient(withInterceptorsFromDi()),
        provideHttpClientTesting(),
      ],
    });
    fixture = TestBed.createComponent(DeploymentComponent);
    component = fixture.componentInstance;
  });
  it('should init vars with route params on ngOnInit', () => {
    // given // when
    component.ngOnInit();
    mockRoute.params.next({
      appserverName: 'aServer',
      releaseName: 'aRelease',
    });
    // then
    expect(component.appserverName).toEqual('aServer');
    expect(component.releaseName).toEqual('aRelease');
    expect(component.deploymentId).toBeUndefined();
    expect(component.isRedeployment()).toBeFalsy();
  });

  it('should init vars with corrected route params on ngOnInit', () => {
    // given // when
    component.ngOnInit();
    mockRoute.params.next({
      appserverName: 'aServer',
    });
    // then
    expect(component.appserverName).toEqual('aServer');
    expect(component.deploymentId).toBeUndefined();
    expect(component.isRedeployment()).toBeFalsy();
  });
});

describe('DeploymentComponent (redeployment)', () => {
  let component: DeploymentComponent;
  let fixture: ComponentFixture<DeploymentComponent>;
  let resourceService: ResourceService;
  let deploymentService: DeploymentService;
  let environmentService: EnvironmentService;

  const mockRoute: any = { snapshot: {} };

  mockRoute.params = new Subject<any>();
  mockRoute.queryParams = new Subject<any>();

  beforeEach(() => {
    TestBed.configureTestingModule({
      teardown: { destroyAfterEach: false },
      imports: [FormsModule, DeploymentComponent, DummyComponent],
      providers: [
        { provide: ActivatedRoute, useValue: mockRoute },
        ResourceService,
        EnvironmentService,
        DeploymentService,
        provideHttpClient(withInterceptorsFromDi()),
        provideHttpClientTesting(),
      ],
    });
    fixture = TestBed.createComponent(DeploymentComponent);
    component = fixture.componentInstance;

    resourceService = TestBed.inject(ResourceService);
    deploymentService = TestBed.inject(DeploymentService);
    environmentService = TestBed.inject(EnvironmentService);
    component.transDeploymentParameters = [];
  });

  it('should call deploymentService on ngOnInit', async () => {
    // given
    const deployment: Deployment = { appsWithVersion: [] } as Deployment;
    vi.spyOn(environmentService, 'getAll').mockReturnValue(of([]));
    vi.spyOn(deploymentService, 'get').mockReturnValue(of(deployment));
    expect(deploymentService.get).not.toHaveBeenCalled();

    // when
    component.ngOnInit();
    mockRoute.params.next({
      deploymentId: 123,
    });
    await fixture.whenStable();
    fixture.detectChanges();

    // then
    expect(deploymentService.get).toHaveBeenCalledWith(123);
  });

  it('should initRedeploymentValues ngOnInit and leave isDeploymentBlocked to false if all appWithVersion have been found', async () => {
    // given
    component.environments.set([{ name: 'X', id: 1 } as Environment, { name: 'Y', id: 2 } as Environment]);
    const appsWithVersion: AppWithVersion[] = [
      {
        applicationId: 4,
        applicationName: 'testApp',
        version: '1.2.3',
      } as AppWithVersion,
      {
        applicationId: 5,
        applicationName: 'testAPP',
        version: '1.2.3.4',
      } as AppWithVersion,
    ];
    const deploymentParameter: DeploymentParameter = {
      key: 'atest',
      value: 'foo',
    } as DeploymentParameter;
    const deployment: Deployment = {
      appsWithVersion,
      deploymentParameters: [deploymentParameter],
      releaseName: 'testRelease',
      appServerName: 'testServer',
      environmentName: 'Y',
    } as Deployment;

    vi.spyOn(deploymentService, 'get').mockReturnValue(of(deployment));
    vi.spyOn(environmentService, 'getAll').mockReturnValue(
      of([{ name: 'X', id: 1 } as Environment, { name: 'Y', id: 2 } as Environment]),
    );
    vi.spyOn(resourceService, 'getDeployableReleases').mockReturnValue(
      of([{ id: 9, release: 'testRelease' } as Release]),
    );
    vi.spyOn(resourceService, 'getAppsWithVersions').mockReturnValue(
      of([
        {
          applicationId: 4,
          applicationName: 'testApp',
          version: '1.2.3',
        } as AppWithVersion,
        {
          applicationId: 5,
          applicationName: 'testAPP',
          version: '1.2.3.4',
        } as AppWithVersion,
      ]),
    );
    // when
    component.ngOnInit();
    mockRoute.params.next({
      deploymentId: 123,
    });
    await fixture.whenStable();
    fixture.detectChanges();

    // then
    expect(component.isRedeployment()).toBeTruthy();
    expect(component.isDeploymentBlocked()).toBeFalsy();
    expect(component.selectedRelease().release).toEqual('testRelease');
    expect(component.selectedAppserver().name).toEqual('testServer');
    expect(component.appsWithVersion()).toEqual(appsWithVersion);
    expect(component.transDeploymentParameters).toEqual([deploymentParameter]);
    expect(component.redeploymentAppserverDisplayName()).toContain('testServer');
    expect(component.redeploymentAppserverDisplayName()).toContain('testRelease');
    expect(component.environments()[0].selected).toBeFalsy();
    expect(component.environments()[1].selected).toBeTruthy();
  });

  it('should initRedeploymentValues and set isDeploymentBlocked to true if an appWithVersion is missing ngOnInit', async () => {
    // given
    component.environments.set([{ name: 'X', id: 1 } as Environment, { name: 'Y', id: 2 } as Environment]);
    const appsWithVersion: AppWithVersion[] = [
      {
        applicationId: 4,
        applicationName: 'testApp',
        version: '1.2.3',
      } as AppWithVersion,
      {
        applicationId: 5,
        applicationName: 'testAPP',
        version: '1.2.3.4',
      } as AppWithVersion,
    ];
    const deploymentParameter: DeploymentParameter = {
      key: 'atest',
      value: 'foo',
    } as DeploymentParameter;
    const deployment: Deployment = {
      appsWithVersion,
      deploymentParameters: [deploymentParameter],
      releaseName: 'testRelease',
      appServerName: 'testServer',
      appServerId: 1,
      environmentName: 'Y',
    } as Deployment;
    vi.spyOn(deploymentService, 'get').mockReturnValue(of(deployment));
    vi.spyOn(environmentService, 'getAll').mockReturnValue(
      of([{ name: 'X', id: 1 } as Environment, { name: 'Y', id: 2 } as Environment]),
    );
    vi.spyOn(resourceService, 'getDeployableReleases').mockReturnValue(
      of([{ id: 9, release: 'testRelease' } as Release]),
    );
    // second app missing
    vi.spyOn(resourceService, 'getAppsWithVersions').mockReturnValue(
      of([
        {
          applicationId: 4,
          applicationName: 'testApp',
          version: '1.2.3',
        } as AppWithVersion,
      ]),
    );
    // when
    component.ngOnInit();
    mockRoute.params.next({
      deploymentId: 123,
    });
    await fixture.whenStable();
    fixture.detectChanges();
    // then
    expect(component.isRedeployment()).toBeTruthy();
    expect(component.isDeploymentBlocked()).toBeTruthy();
  });

  it('should call the deploymentService with the right values on createDeployment', async () => {
    // given
    component.environments.set([{ name: 'X', id: 1 } as Environment, { name: 'Y', id: 2 } as Environment]);
    const appsWithVersion: AppWithVersion[] = [
      {
        applicationId: 4,
        applicationName: 'testApp',
        version: '1.2.3',
      } as AppWithVersion,
      {
        applicationId: 5,
        applicationName: 'testAPP',
        version: '1.2.3.4',
      } as AppWithVersion,
    ];
    const deploymentParameters: DeploymentParameter[] = [
      { key: 'atest', value: 'foo' } as DeploymentParameter,
      { key: 'btest', value: 'bar' } as DeploymentParameter,
    ];
    const deployment: Deployment = {
      appsWithVersion,
      deploymentParameters,
      releaseName: 'testRelease',
      appServerName: 'testServer',
      environmentName: 'Y',
    } as Deployment;
    vi.spyOn(deploymentService, 'get').mockReturnValue(of(deployment));
    vi.spyOn(environmentService, 'getAll').mockReturnValue(
      of([{ name: 'X', id: 1 } as Environment, { name: 'Y', id: 2 } as Environment]),
    );
    const deploymentRequest: DeploymentRequest = {
      appServerName: 'testServer',
      releaseName: 'testRelease',
      contextIds: [2],
      simulate: false,
      sendEmail: false,
      requestOnly: false,
      appsWithVersion,
      deploymentParameters,
    } as DeploymentRequest;
    vi.spyOn(resourceService, 'getDeployableReleases').mockReturnValue(
      of([{ id: 9, release: 'testRelease' } as Release]),
    );
    vi.spyOn(deploymentService, 'createDeployment').mockReturnValue(of({ trackingId: 911 } as Deployment));
    // when
    component.ngOnInit();
    mockRoute.params.next({
      deploymentId: 123,
    });
    fixture.detectChanges();
    await fixture.whenStable();
    component.createDeployment();
    // then
    expect(deploymentService.createDeployment).toHaveBeenCalledWith(deploymentRequest);
    expect(component.successMessage()).toContain('Tracking Id 911');
  });
});
