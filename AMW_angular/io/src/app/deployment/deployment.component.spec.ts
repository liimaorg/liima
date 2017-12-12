import { Component } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { inject, TestBed } from '@angular/core/testing';
import { BaseRequestOptions, ConnectionBackend, Http } from '@angular/http';
import { MockBackend } from '@angular/http/testing';
import { CommonModule } from '@angular/common';
import { RouterTestingModule } from '@angular/router/testing';
import { DeploymentComponent } from './deployment.component';
import { DeploymentService } from './deployment.service';
import { EnvironmentService } from './environment.service';
import { ResourceService } from '../resource/resource.service';
import { Observable } from 'rxjs';
import { Environment } from './environment';
import { Resource } from '../resource/resource';
import { Release } from '../resource/release';
import { AppState } from '../app.service';
import { DeploymentRequest } from './deployment-request';
import { DeploymentParameter } from './deployment-parameter';
import { Deployment } from './deployment';
import { ResourceTag } from '../resource/resource-tag';
import { AppWithVersion } from './app-with-version';

@Component({
  template: ''
})
class DummyComponent {
}

describe('DeploymentComponent (create deployment)', () => {
  // provide our implementations or mocks to the dependency injector
  beforeEach(() => TestBed.configureTestingModule({
    imports: [
      CommonModule,
      RouterTestingModule.withRoutes([
        {path: 'deployment', component: DummyComponent}
      ])
    ],
    providers: [
      BaseRequestOptions,
      MockBackend,
      {
        provide: Http,
        useFactory(backend: ConnectionBackend, defaultOptions: BaseRequestOptions) {
          return new Http(backend, defaultOptions);
        },
        deps: [MockBackend, BaseRequestOptions]
      },
      EnvironmentService,
      DeploymentService,
      ResourceService,
      DeploymentComponent,
      AppState,
    ],
    declarations: [DummyComponent],
  }));

  it('should have default data', inject([DeploymentComponent], (deploymentComponent: DeploymentComponent) => {
    // given when then
    expect(deploymentComponent.appserverName).toEqual('');
    expect(deploymentComponent.releaseName).toEqual('');
    expect(deploymentComponent.deploymentId).toBeUndefined();
    expect(deploymentComponent.isLoading).toBeFalsy();
    expect(deploymentComponent.isRedeployment).toBeFalsy();
  }));

  it('should not be ready for deployment', inject([DeploymentComponent], (deploymentComponent: DeploymentComponent) => {
    // given
    expect(deploymentComponent.isReadyForDeployment()).toBeFalsy();
    // when
    deploymentComponent.ngOnInit();
    // then
    expect(deploymentComponent.isReadyForDeployment()).toBeFalsy();
  }));

  it('should call resourceService on ngOnInit', inject([DeploymentComponent, ResourceService],
    (deploymentComponent: DeploymentComponent, resourceService: ResourceService) => {
    // given
    spyOn(resourceService, 'getByType').and.returnValue(Observable.of([]));
    expect(resourceService.getByType).not.toHaveBeenCalled();
    // when
    deploymentComponent.ngOnInit();
    // then
    expect(resourceService.getByType).toHaveBeenCalled();
  }));

  it('should call environmentService on ngOnInit', inject([DeploymentComponent, EnvironmentService],
    (deploymentComponent: DeploymentComponent, environmentService: EnvironmentService) => {
    // given
    spyOn(environmentService, 'getAll').and.returnValue(Observable.of([]));
    expect(environmentService.getAll).not.toHaveBeenCalled();
    // when
    deploymentComponent.ngOnInit();
    // then
    expect(environmentService.getAll).toHaveBeenCalled();
  }));

  it('should populate groupedEnvironments on ngOnInit', inject([DeploymentComponent, EnvironmentService],
    (deploymentComponent: DeploymentComponent, environmentService: EnvironmentService) => {
    // given
    const environments: Environment[] = [{id: 1, name: 'A', parent: 'DEV'} as Environment];
    spyOn(environmentService, 'getAll').and.returnValue(Observable.of(environments));
    expect(deploymentComponent.groupedEnvironments).toEqual({});
    // when
    deploymentComponent.ngOnInit();
    // then
    expect(deploymentComponent.groupedEnvironments['DEV'].length).toBe(1);
  }));

  it('should call deploymentService on ngAfterViewInit', inject([DeploymentComponent, DeploymentService],
    (deploymentComponent: DeploymentComponent, deploymentService: DeploymentService) => {
    // given
    spyOn(deploymentService, 'getAllDeploymentParameterKeys').and.returnValue(Observable.of([]));
    expect(deploymentService.getAllDeploymentParameterKeys).not.toHaveBeenCalled();
    // when
    deploymentComponent.ngAfterViewInit();
    // then
    expect(deploymentService.getAllDeploymentParameterKeys).toHaveBeenCalled();
  }));

  it('should set selectedAppserver and selectedRelease', inject([DeploymentComponent, ResourceService],
    (deploymentComponent: DeploymentComponent, resourceService: ResourceService) => {
    // given
    const testReleases: Release[] = [{id: 1, release: 'testRelease'} as Release,
      {id: 2, release: 'anotherRelease'} as Release];
    deploymentComponent.appserverName = 'testServer';
    deploymentComponent.releaseName = 'testRelease';
    spyOn(resourceService, 'getByType').and.returnValue(Observable.of([{
      name: 'testServer',
      releases: testReleases
    } as Resource]));
    spyOn(resourceService, 'getDeployableReleases').and.returnValue(Observable.of(testReleases));
    // when
    deploymentComponent.initAppservers();
    // then
    expect(deploymentComponent.selectedAppserver.name).toBe('testServer');
    expect(deploymentComponent.selectedRelease.release).toBe('testRelease');
  }));

  it('should set selectedAppserver but not set selectedRelease if not available for selectedAppserver',
    inject([DeploymentComponent, ResourceService], (deploymentComponent: DeploymentComponent, resourceService: ResourceService) => {
      // given
      const testReleases: Release[] = [{id: 1, release: 'testRelease'} as Release];
      deploymentComponent.appserverName = 'testServer';
      deploymentComponent.releaseName = 'missingRelease';
      spyOn(resourceService, 'getByType').and.returnValue(Observable.of([{
        name: 'testServer',
        releases: testReleases
      } as Resource]));
      // when
      deploymentComponent.initAppservers();
      // then
      expect(deploymentComponent.selectedAppserver.name).toBe('testServer');
      expect(deploymentComponent.selectedRelease).toBeNull();
  }));

  it('should return environementGroupNames on getEnvironmentGroups()',
    inject([DeploymentComponent, EnvironmentService], (deploymentComponent: DeploymentComponent, environmentService: EnvironmentService) => {
    // given
    const environments: Environment[] = [{id: 1, name: 'A', parent: 'DEV'} as Environment,
      {id: 2, name: 'B', parent: 'DEV'} as Environment, {id: 3, name: 'P', parent: 'PROD'} as Environment];
    spyOn(environmentService, 'getAll').and.returnValue(Observable.of(environments));
    deploymentComponent.ngOnInit();
    // when
    const groups: string[] = deploymentComponent.getEnvironmentGroups();
    // then
    expect(groups.length).toBe(2);
    expect(groups).toContain('DEV', 'PROD');
  }));

  it('should keep environments selected on onChangeAppserver',
    inject([DeploymentComponent], (deploymentComponent: DeploymentComponent) => {
    // given
    deploymentComponent.selectedRelease = {id: 1} as Release;
    const appServer: Resource = {name: 'testServer'} as Resource;
    deploymentComponent.environments = [{id: 1} as Environment, {id: 2, selected: true} as Environment];
    deploymentComponent.selectedAppserver = appServer;
    // when
    deploymentComponent.onChangeAppserver();
    // then
    expect(deploymentComponent.selectedRelease).toBeNull();
    expect(deploymentComponent.environments[0].selected).toBeFalsy();
    expect(deploymentComponent.environments[1].selected).toBeTruthy();
  }));

  it('should check permission on onChangeAppserver',
    inject([DeploymentComponent, DeploymentService, ResourceService],
      (deploymentComponent: DeploymentComponent, deploymentService: DeploymentService, resourceService: ResourceService) => {
      // given
      deploymentComponent.selectedRelease = {id: 1} as Release;
      const appServer: Resource = {name: 'testServer', id: 3} as Resource;
      deploymentComponent.environments = [{id: 1} as Environment, {id: 2, selected: true} as Environment];
      deploymentComponent.selectedAppserver = appServer;
      spyOn(resourceService, 'canCreateShakedownTest').and.returnValue(Observable.of(false));
      spyOn(deploymentService, 'canDeploy').and.returnValue(Observable.of(true));
      // when
      deploymentComponent.onChangeAppserver();
      // then
      expect(resourceService.canCreateShakedownTest).toHaveBeenCalledWith(3);
      expect(deploymentService.canDeploy).toHaveBeenCalledWith(3, [ 2 ]);
      expect(deploymentComponent.hasPermissionShakedownTest).toBeFalsy();
      expect(deploymentComponent.hasPermissionToDeploy).toBeTruthy();
    }));

  it('should call resourceService on onChangeRelease',
    inject([DeploymentComponent, ResourceService], (deploymentComponent: DeploymentComponent, resourceService: ResourceService) => {
    // given
    const testRelease: Release = {id: 1, release: 'testRelease'} as Release;
    const betterRelease: Release = {id: 1, release: 'betterRelease'} as Release;
    deploymentComponent.releases = [testRelease];
    deploymentComponent.selectedRelease = testRelease;
    spyOn(resourceService, 'getLatestForRelease').and.returnValue(Observable.of(betterRelease));
    spyOn(resourceService, 'getAppsWithVersions').and.returnValue(Observable.of(''));
    deploymentComponent.selectedAppserver = {name: 'testServer', releases: [testRelease]} as Resource;
    // when
    deploymentComponent.onChangeRelease();
    // then
    expect(resourceService.getLatestForRelease).toHaveBeenCalled();
    expect(resourceService.getAppsWithVersions).toHaveBeenCalled();
  }));

  it('should replace deploymentParameters with same key onAddParam',
    inject([DeploymentComponent], (deploymentComponent: DeploymentComponent) => {
    // given
    deploymentComponent.transDeploymentParameters = [{key: 'atest', value: 'foo'} as DeploymentParameter];
    deploymentComponent.transDeploymentParameter = {key: 'atest', value: 'bar'} as DeploymentParameter;
    // when
    deploymentComponent.onAddParam();
    // then
    expect(deploymentComponent.transDeploymentParameters.length).toBe(1);
    expect(deploymentComponent.transDeploymentParameters[0].value).toEqual('bar');
  }));

  it('should remove the right deploymentParameter onRemoveParam',
    inject([DeploymentComponent], (deploymentComponent: DeploymentComponent) => {
    // given
    const parameterA: DeploymentParameter = {key: 'atest', value: 'foo'} as DeploymentParameter;
    const parameterB: DeploymentParameter = {key: 'btest', value: 'bar'} as DeploymentParameter;
    deploymentComponent.transDeploymentParameters = [ parameterA, parameterB ];
    // when
    deploymentComponent.onRemoveParam(parameterA);
    // then
    expect(deploymentComponent.transDeploymentParameters.length).toBe(1);
    expect(deploymentComponent.transDeploymentParameters[0].value).toEqual(parameterB.value);
  }));

  it('should not be readyForDeployment if no environment is selected',
    inject([DeploymentComponent], (deploymentComponent: DeploymentComponent) => {
    // given
    deploymentComponent.selectedAppserver = {name: 'testServer'} as Resource;
    deploymentComponent.selectedRelease = {id: 1, release: 'testRelease'} as Release;
    deploymentComponent.environments = [{id: 1} as Environment];
    // when then
    expect(deploymentComponent.isReadyForDeployment()).toBeFalsy();
  }));

  it('should not be readyForDeployment if a release ist set, an environment is selected but appWithVersions is empty',
    inject([DeploymentComponent], (deploymentComponent: DeploymentComponent) => {
      // given
      deploymentComponent.selectedAppserver = {name: 'testServer'} as Resource;
      deploymentComponent.selectedRelease = {id: 1, release: 'testRelease'} as Release;
      deploymentComponent.environments = [{id: 1} as Environment, {id: 2, selected: true} as Environment];
      // when then
      expect(deploymentComponent.isReadyForDeployment()).toBeFalsy();
    }));

  it('should be readyForDeployment if a release ist set, an environment is selected and appWithVersions is not empty',
    inject([DeploymentComponent], (deploymentComponent: DeploymentComponent) => {
    // given
    deploymentComponent.selectedAppserver = {name: 'testServer'} as Resource;
    deploymentComponent.selectedRelease = {id: 1, release: 'testRelease'} as Release;
    deploymentComponent.environments = [{id: 1} as Environment, {id: 2, selected: true} as Environment];
    deploymentComponent.appsWithVersion = [{applicationName: 'testApp'} as AppWithVersion];
    // when then
    expect(deploymentComponent.isReadyForDeployment()).toBeTruthy();
  }));

  it('should call the deploymentService with the right values on requestDeployment',
    inject([DeploymentComponent, DeploymentService], (deploymentComponent: DeploymentComponent, deploymentService: DeploymentService) => {
    // given
    deploymentComponent.selectedAppserver = {name: 'testServer'} as Resource;
    deploymentComponent.selectedRelease = {id: 1, release: 'testRelease'} as Release;
    deploymentComponent.environments = [{id: 2, name: 'A'} as Environment, {id: 3, name: 'B', selected: true} as Environment];
    deploymentComponent.doExecuteShakedownTest = true;
    deploymentComponent.appsWithVersion = [{applicationId: 4, applicationName: 'testApp', version: '1.2.3'} as AppWithVersion];
    deploymentComponent.selectedResourceTag = {id: 5, tagDate: 1485378084103} as ResourceTag;
    deploymentComponent.deploymentDate = '02.01.2017 12:00';
    deploymentComponent.transDeploymentParameters = [{key: 'atest', value: 'foo'} as DeploymentParameter,
      {key: 'btest', value: 'bar'} as DeploymentParameter];
    const deploymentRequest: DeploymentRequest = {appServerName: 'testServer', contextIds: [3],
      releaseName: 'testRelease', simulate: false, sendEmail: false, executeShakedownTest: deploymentComponent.doExecuteShakedownTest,
      neighbourhoodTest: false, requestOnly: true,  appsWithVersion: deploymentComponent.appsWithVersion,
      stateToDeploy: deploymentComponent.selectedResourceTag.tagDate, deploymentDate: 1483354800000,
      deploymentParameters: deploymentComponent.transDeploymentParameters} as DeploymentRequest;
    spyOn(deploymentService, 'createDeployment').and.returnValue(Observable.of({ trackingId: 910 } as Deployment));
    // when
    deploymentComponent.requestDeployment();
    // then
    expect(deploymentService.createDeployment).toHaveBeenCalledWith(deploymentRequest);
    expect(deploymentComponent.successMessage).toContain('Tracking Id 910');
  }));

  it('should call the deploymentService with the right values on createDeployment',
    inject([DeploymentComponent, DeploymentService], (deploymentComponent: DeploymentComponent, deploymentService: DeploymentService) => {
    // given
    deploymentComponent.selectedAppserver = {name: 'testServer'} as Resource;
    deploymentComponent.selectedRelease = {id: 1, release: 'testRelease'} as Release;
    deploymentComponent.environments = [{id: 2, name: 'A', selected: true} as Environment, {id: 3, name: 'B', selected: true} as Environment];
    deploymentComponent.doExecuteShakedownTest = true;
    deploymentComponent.simulate = true;
    deploymentComponent.appsWithVersion = [{applicationId: 4, applicationName: 'testApp', version: '1.2.3'} as AppWithVersion,
      {applicationId: 5, applicationName: 'testAPP', version: '1.2.3.4'} as AppWithVersion];
    deploymentComponent.selectedResourceTag = {id: 5, tagDate: 1485378084103} as ResourceTag;
    deploymentComponent.transDeploymentParameters = [{key: 'atest', value: 'foo'} as DeploymentParameter,
      {key: 'btest', value: 'bar'} as DeploymentParameter];
    const deploymentRequest: DeploymentRequest = {appServerName: 'testServer', contextIds: [2, 3],
      releaseName: 'testRelease', simulate: deploymentComponent.simulate, sendEmail: false, executeShakedownTest: deploymentComponent.doExecuteShakedownTest,
      neighbourhoodTest: false, requestOnly: false,  appsWithVersion: deploymentComponent.appsWithVersion,
      stateToDeploy: deploymentComponent.selectedResourceTag.tagDate, deploymentParameters: deploymentComponent.transDeploymentParameters} as DeploymentRequest;
    spyOn(deploymentService, 'createDeployment').and.returnValue(Observable.of({trackingId: 911} as Deployment));
    // when
    deploymentComponent.createDeployment();
    // then
    expect(deploymentService.createDeployment).toHaveBeenCalledWith(deploymentRequest);
    expect(deploymentComponent.successMessage).toContain('Tracking Id 911');
  }));

});

describe('DeploymentComponent (create deployment with params)', () => {
  beforeEach(() => TestBed.configureTestingModule({
    imports: [
      CommonModule,
      RouterTestingModule.withRoutes([
        {path: 'deployment', component: DummyComponent}
      ])
    ],
    providers: [
      BaseRequestOptions, {
        provide: ActivatedRoute,
        useValue: {
          params: Observable.of({appserverName: 'aServer', releaseName: 'aRelease'})
        },
      },
      MockBackend,
      {
        provide: Http,
        useFactory(backend: ConnectionBackend, defaultOptions: BaseRequestOptions) {
          return new Http(backend, defaultOptions);
        },
        deps: [MockBackend, BaseRequestOptions]
      },
      EnvironmentService,
      DeploymentService,
      ResourceService,
      DeploymentComponent,
      AppState,
    ],
    declarations: [DummyComponent],
  }));

  it('should init vars with route params on ngOnInit', inject([DeploymentComponent], (deploymentComponent: DeploymentComponent) => {
    // given // when
    deploymentComponent.ngOnInit();
    // then
    expect(deploymentComponent.appserverName).toEqual('aServer');
    expect(deploymentComponent.releaseName).toEqual('aRelease');
    expect(deploymentComponent.deploymentId).toBeUndefined();
    expect(deploymentComponent.isRedeployment).toBeFalsy();
  }));

});

describe('DeploymentComponent (create deployment with fake redeploy param)', () => {
  beforeEach(() => TestBed.configureTestingModule({
    imports: [
      CommonModule,
      RouterTestingModule.withRoutes([
        {path: 'deployment', component: DummyComponent}
      ])
    ],
    providers: [
      BaseRequestOptions, {
        provide: ActivatedRoute,
        useValue: {
          params: Observable.of({deploymentId: 'aServer'})
        },
      },
      MockBackend,
      {
        provide: Http,
        useFactory(backend: ConnectionBackend, defaultOptions: BaseRequestOptions) {
          return new Http(backend, defaultOptions);
        },
        deps: [MockBackend, BaseRequestOptions]
      },
      EnvironmentService,
      DeploymentService,
      ResourceService,
      DeploymentComponent,
      AppState,
    ],
    declarations: [DummyComponent],
  }));

  it('should init vars with corrected route params on ngOnInit', inject([DeploymentComponent], (deploymentComponent: DeploymentComponent) => {
    // given // when
    deploymentComponent.ngOnInit();
    // then
    expect(deploymentComponent.appserverName).toEqual('aServer');
    expect(deploymentComponent.deploymentId).toBeUndefined();
    expect(deploymentComponent.isRedeployment).toBeFalsy();
  }));

});

describe('DeploymentComponent (redeployment)', () => {
  beforeEach(() => TestBed.configureTestingModule({
    imports: [
      CommonModule,
      RouterTestingModule.withRoutes([
        {path: 'deployment', component: DummyComponent}
      ])
    ],
    providers: [
      BaseRequestOptions, {
        provide: ActivatedRoute,
        useValue: {
          params: Observable.of({ deploymentId: 123 })
        },
      },
      MockBackend,
      {
        provide: Http,
        useFactory(backend: ConnectionBackend, defaultOptions: BaseRequestOptions) {
          return new Http(backend, defaultOptions);
        },
        deps: [MockBackend, BaseRequestOptions]
      },
      EnvironmentService,
      DeploymentService,
      ResourceService,
      DeploymentComponent,
      AppState,
    ],
    declarations: [DummyComponent],
  }));

  it('should call deploymentService on ngOnInit', inject([DeploymentComponent, DeploymentService],
    (deploymentComponent: DeploymentComponent, deploymentService: DeploymentService) => {
    // given
    const deployment: Deployment = {appsWithVersion: []} as Deployment;
    spyOn(deploymentService, 'get').and.returnValue(Observable.of(deployment));
    expect(deploymentService.get).not.toHaveBeenCalled();
    // when
    deploymentComponent.ngOnInit();
    // then
    expect(deploymentService.get).toHaveBeenCalledWith(123);
  }));

  it('should initRedeploymentValues ngOnInit and leave isDeploymentBlocked to false if all appWithVersion have been found',
    inject([DeploymentComponent, DeploymentService, ResourceService],
    (deploymentComponent: DeploymentComponent, deploymentService: DeploymentService, resourceService: ResourceService) => {
    // given
    deploymentComponent.environments = [{name: 'X'} as Environment, {name: 'Y'} as Environment];
    const appsWithVersion: AppWithVersion[] = [{applicationId: 4, applicationName: 'testApp', version: '1.2.3'} as AppWithVersion,
      {applicationId: 5, applicationName: 'testAPP', version: '1.2.3.4'} as AppWithVersion];
    const deploymentParameter: DeploymentParameter = {key: 'atest', value: 'foo'} as DeploymentParameter;
    const deployment: Deployment = {appsWithVersion, deploymentParameters: [deploymentParameter],
      releaseName: 'testRelease', appServerName: 'testServer', environmentName: 'Y'} as Deployment;
    spyOn(deploymentService, 'get').and.returnValue(Observable.of(deployment));
    spyOn(resourceService, 'getDeployableReleases').and.returnValue(Observable.of([{ id: 9, release: 'testRelease' } as Release]));
    spyOn(resourceService, 'getAppsWithVersions').and.returnValue(Observable.of([{applicationId: 4, applicationName: 'testApp', version: '1.2.3'} as AppWithVersion,
        {applicationId: 5, applicationName: 'testAPP', version: '1.2.3.4'} as AppWithVersion]));
    // when
    deploymentComponent.ngOnInit();
    // then
    expect(deploymentComponent.isRedeployment).toBeTruthy();
    expect(deploymentComponent.isDeploymentBlocked).toBeFalsy();
    expect(deploymentComponent.selectedRelease.release).toEqual('testRelease');
    expect(deploymentComponent.selectedAppserver.name).toEqual('testServer');
    expect(deploymentComponent.appsWithVersion).toEqual(appsWithVersion);
    expect(deploymentComponent.transDeploymentParameters).toEqual([deploymentParameter]);
    expect(deploymentComponent.redeploymentAppserverDisplayName).toContain('testServer');
    expect(deploymentComponent.redeploymentAppserverDisplayName).toContain('testRelease');
    expect(deploymentComponent.environments[0].selected).toBeFalsy();
    expect(deploymentComponent.environments[1].selected).toBeTruthy();
  }));

  it('should initRedeploymentValues and set isDeploymentBlocked to true if an appWithVersion is missing ngOnInit',
    inject([DeploymentComponent, DeploymentService, ResourceService],
    (deploymentComponent: DeploymentComponent, deploymentService: DeploymentService, resourceService: ResourceService) => {
      // given
      deploymentComponent.environments = [{name: 'X'} as Environment, {name: 'Y'} as Environment];
      const appsWithVersion: AppWithVersion[] = [{applicationId: 4, applicationName: 'testApp', version: '1.2.3'} as AppWithVersion,
        {applicationId: 5, applicationName: 'testAPP', version: '1.2.3.4'} as AppWithVersion];
      const deploymentParameter: DeploymentParameter = {key: 'atest', value: 'foo'} as DeploymentParameter;
      const deployment: Deployment = {appsWithVersion, deploymentParameters: [deploymentParameter],
        releaseName: 'testRelease', appServerName: 'testServer', appServerId: 1, environmentName: 'Y'} as Deployment;
      spyOn(deploymentService, 'get').and.returnValue(Observable.of(deployment));
      spyOn(resourceService, 'getDeployableReleases').and.returnValue(Observable.of([{id: 9, release: 'testRelease'} as Release]));
      // second app missing
      spyOn(resourceService, 'getAppsWithVersions').and.returnValue(Observable.of([{applicationId: 4, applicationName: 'testApp', version: '1.2.3'} as AppWithVersion]));
      // when
      deploymentComponent.ngOnInit();
      // then
      expect(deploymentComponent.isRedeployment).toBeTruthy();
      expect(deploymentComponent.isDeploymentBlocked).toBeTruthy();
  }));

  it('should call the deploymentService with the right values on createDeployment', inject([DeploymentComponent, DeploymentService, ResourceService],
      (deploymentComponent: DeploymentComponent, deploymentService: DeploymentService, resourceService: ResourceService) => {
      // given
      deploymentComponent.environments = [{name: 'X', id: 1} as Environment, {name: 'Y', id: 2} as Environment];
      const appsWithVersion: AppWithVersion[] = [{applicationId: 4, applicationName: 'testApp', version: '1.2.3'} as AppWithVersion,
        {applicationId: 5, applicationName: 'testAPP', version: '1.2.3.4'} as AppWithVersion];
      const deploymentParameters: DeploymentParameter[] = [{key: 'atest', value: 'foo'} as DeploymentParameter,
        {key: 'btest', value: 'bar'} as DeploymentParameter];
      const deployment: Deployment = {appsWithVersion, deploymentParameters,
        releaseName: 'testRelease', appServerName: 'testServer', environmentName: 'Y'} as Deployment;
      spyOn(deploymentService, 'get').and.returnValue(Observable.of(deployment));
      const deploymentRequest: DeploymentRequest = {appServerName: 'testServer', releaseName: 'testRelease',
        contextIds: [2], simulate: false, sendEmail: false, executeShakedownTest: false, neighbourhoodTest: false,
        requestOnly: false,  appsWithVersion, deploymentParameters} as DeploymentRequest;
      spyOn(resourceService, 'getDeployableReleases').and.returnValue(Observable.of([{id: 9, release: 'testRelease'} as Release]));
      spyOn(deploymentService, 'createDeployment').and.returnValue(Observable.of({trackingId: 911} as Deployment));
      // when
      deploymentComponent.ngOnInit();
      deploymentComponent.createDeployment();
      // then
      expect(deploymentService.createDeployment).toHaveBeenCalledWith(deploymentRequest);
      expect(deploymentComponent.successMessage).toContain('Tracking Id 911');
    }));

});
