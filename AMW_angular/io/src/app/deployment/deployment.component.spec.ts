///<reference path="../resource/release.ts"/>
import { Component } from '@angular/core';
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
import {ResourceTag} from "../resource/resource-tag";
import {AppWithVersion} from "./app-with-version";


@Component({
  template: ''
})
class DummyComponent {
}

describe('DeploymentComponent', () => {
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
        useFactory: function (backend: ConnectionBackend, defaultOptions: BaseRequestOptions) {
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
    expect(deploymentComponent.isLoading).toBeFalsy();
  }));

  it('should log ngOnInit', inject([DeploymentComponent], (deploymentComponent: DeploymentComponent) => {
    // given
    spyOn(console, 'log');
    expect(console.log).not.toHaveBeenCalled();
    // when
    deploymentComponent.ngOnInit();
    // then
    expect(console.log).toHaveBeenCalled();
  }));

  it('should not be ready for deployment', inject([DeploymentComponent], (deploymentComponent: DeploymentComponent) => {
    // given
    expect(deploymentComponent.isReadyForDeployment()).toBeFalsy();
    // when
    deploymentComponent.ngOnInit();
    // then
    expect(deploymentComponent.isReadyForDeployment()).toBeFalsy();
  }));

  it('should call resourceService on ngOnInit', inject([DeploymentComponent, ResourceService], (deploymentComponent: DeploymentComponent, resourceService: ResourceService) => {
    // given
    spyOn(resourceService, 'getByType').and.returnValue(Observable.of(''));
    expect(resourceService.getByType).not.toHaveBeenCalled();
    // when
    deploymentComponent.ngOnInit();
    // then
    expect(resourceService.getByType).toHaveBeenCalled();
  }));

  it('should call deploymentService on ngOnInit', inject([DeploymentComponent, DeploymentService], (deploymentComponent: DeploymentComponent, deploymentService: DeploymentService) => {
    // given
    spyOn(deploymentService, 'getAllDeploymentParameterKeys').and.returnValue(Observable.of([]));
    expect(deploymentService.getAllDeploymentParameterKeys).not.toHaveBeenCalled();
    // when
    deploymentComponent.ngOnInit();
    // then
    expect(deploymentService.getAllDeploymentParameterKeys).toHaveBeenCalled();
  }));

  it('should call environmentService on ngOnInit', inject([DeploymentComponent, EnvironmentService], (deploymentComponent: DeploymentComponent, environmentService: EnvironmentService) => {
    // given
    spyOn(environmentService, 'getAll').and.returnValue(Observable.of([]));
    expect(environmentService.getAll).not.toHaveBeenCalled();
    // when
    deploymentComponent.ngOnInit();
    // then
    expect(environmentService.getAll).toHaveBeenCalled();
  }));

  it('should populate environmentGroups on ngOnInit', inject([DeploymentComponent, EnvironmentService], (deploymentComponent: DeploymentComponent, environmentService: EnvironmentService) => {
    // given
    let environments: Environment[] = [<Environment>{id: 1, name: 'A', parent: 'DEV'}];
    spyOn(environmentService, 'getAll').and.returnValue(Observable.of(environments));
    expect(deploymentComponent.environmentGroups).toEqual([]);
    // when
    deploymentComponent.ngOnInit();
    // then
    expect(deploymentComponent.environmentGroups).toContain('DEV');
  }));

  it('should set selectedAppserver and selectedRelease', inject([DeploymentComponent, ResourceService], (deploymentComponent: DeploymentComponent, resourceService: ResourceService) => {
    // given
    let testReleases: Release[] = [<Release>{id: 1, release: 'testRelease'},
      <Release>{id: 2, release: 'anotherRelease'}];
    deploymentComponent.appserverName = 'testServer';
    deploymentComponent.releaseName = 'testRelease';
    spyOn(resourceService, 'getByType').and.returnValue(Observable.of([<Resource>{
      name: 'testServer',
      releases: testReleases
    }]));
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
      let testReleases: Release[] = [<Release>{id: 1, release: 'testRelease'}];
      deploymentComponent.appserverName = 'testServer';
      deploymentComponent.releaseName = 'missingRelease';
      spyOn(resourceService, 'getByType').and.returnValue(Observable.of([<Resource>{
        name: 'testServer',
        releases: testReleases
      }]));
      // when
      deploymentComponent.initAppservers();
      // then
      expect(deploymentComponent.selectedAppserver.name).toBe('testServer');
      expect(deploymentComponent.selectedRelease).toBeNull();
    }));

  it('should keep environments selected on onChangeAppserver', inject([DeploymentComponent], (deploymentComponent: DeploymentComponent) => {
    // given
    deploymentComponent.selectedRelease = <Release>{id: 1};
    let appServer: Resource = <Resource>{name: 'testServer'};
    deploymentComponent.environments = [<Environment>{id: 1}, <Environment>{id: 2, selected: true}];
    deploymentComponent.selectedAppserver = appServer;
    // when
    deploymentComponent.onChangeAppserver();
    // then
    expect(deploymentComponent.selectedRelease).toBeNull();
    expect(deploymentComponent.environments[0].selected).toBeFalsy();
    expect(deploymentComponent.environments[1].selected).toBeTruthy();
  }));

  it('should call resourceService on onChangeRelease', inject([DeploymentComponent, ResourceService], (deploymentComponent: DeploymentComponent, resourceService: ResourceService) => {
    // given
    let testRelease: Release = <Release>{id: 1, release: 'testRelease'};
    let betterRelease: Release = <Release>{id: 1, release: 'betterRelease'};
    deploymentComponent.releases = [testRelease];
    deploymentComponent.selectedRelease = testRelease;
    spyOn(resourceService, 'getLatestForRelease').and.returnValue(Observable.of(betterRelease));
    spyOn(resourceService, 'getAppsWithVersions').and.returnValue(Observable.of(''));
    deploymentComponent.selectedAppserver = <Resource>{name: 'testServer', releases: [testRelease]};
    // when
    deploymentComponent.onChangeRelease();
    // then
    expect(resourceService.getLatestForRelease).toHaveBeenCalled();
    expect(resourceService.getAppsWithVersions).toHaveBeenCalled();
  }));

  it('should replace deploymentParameters with same key onAddParam', inject([DeploymentComponent], (deploymentComponent: DeploymentComponent) => {
    // given
    deploymentComponent.transDeploymentParameters = [ <DeploymentParameter>{key:'atest', value:'foo'} ];
    deploymentComponent.transDeploymentParameter = <DeploymentParameter>{key:'atest', value:'bar'};
    // when
    deploymentComponent.onAddParam();
    // then
    expect(deploymentComponent.transDeploymentParameters.length).toBe(1);
    expect(deploymentComponent.transDeploymentParameters[0].value).toEqual('bar');
  }));

  it('should remove the right deploymentParameter onRemoveParam', inject([DeploymentComponent], (deploymentComponent: DeploymentComponent) => {
    // given
    let parameterA: DeploymentParameter = <DeploymentParameter>{key:'atest', value:'foo'};
    let parameterB: DeploymentParameter = <DeploymentParameter>{key:'btest', value:'bar'};
    deploymentComponent.transDeploymentParameters = [ parameterA, parameterB ];
    // when
    deploymentComponent.onRemoveParam(parameterA);
    // then
    expect(deploymentComponent.transDeploymentParameters.length).toBe(1);
    expect(deploymentComponent.transDeploymentParameters[0].value).toEqual(parameterB.value);
  }));

  it('should not be readyForDeployment if no environment is selected', inject([DeploymentComponent], (deploymentComponent: DeploymentComponent) => {
    // given
    deploymentComponent.selectedAppserver = <Resource>{name: 'testServer'};
    deploymentComponent.selectedRelease = <Release>{id: 1, release: 'testRelease'};
    deploymentComponent.environments = [<Environment>{id: 1}];
    // when then
    expect(deploymentComponent.isReadyForDeployment()).toBeFalsy();
  }));

  it('should be readyForDeployment if a release ist set and an environment is selected', inject([DeploymentComponent], (deploymentComponent: DeploymentComponent) => {
    // given
    deploymentComponent.selectedAppserver = <Resource>{name: 'testServer'};
    deploymentComponent.selectedRelease = <Release>{id: 1, release: 'testRelease'};
    deploymentComponent.environments = [<Environment>{id: 1}, <Environment>{id: 2, selected: true}];
    // when then
    expect(deploymentComponent.isReadyForDeployment()).toBeTruthy();
  }));

  it('should call the deploymentService with the right values on requestDeployment', inject([DeploymentComponent, DeploymentService], (deploymentComponent: DeploymentComponent, deploymentService: DeploymentService) => {
    // given
    deploymentComponent.selectedAppserver = <Resource>{name: 'testServer'};
    deploymentComponent.selectedRelease = <Release>{id: 1, release: 'testRelease'};
    deploymentComponent.environments = [<Environment>{id: 2, name: 'A'}, <Environment>{id: 3, name: 'B', selected: true}];
    deploymentComponent.doExecuteShakedownTest = true;
    deploymentComponent.appsWithVersion = [<AppWithVersion>{applicationId: 4, applicationName: 'testApp', version: '1.2.3'}];
    deploymentComponent.selectedResourceTag = <ResourceTag> {id: 5, tagDate: 1485378084103};
    deploymentComponent.transDeploymentParameters = [ <DeploymentParameter>{key:'atest', value:'foo'}, <DeploymentParameter>{key:'btest', value:'bar'} ];
    let deploymentRequest: DeploymentRequest = <DeploymentRequest>{ appServerName: 'testServer', environmentName: 'B',
      releaseName: 'testRelease', sendEmail: false, executeShakedownTest: deploymentComponent.doExecuteShakedownTest,
      neighbourhoodTest: false, requestOnly: true,  appsWithVersion: deploymentComponent.appsWithVersion,
      stateToDeploy: deploymentComponent.selectedResourceTag.tagDate, deploymentDate: null, deploymentParameters: deploymentComponent.transDeploymentParameters};
    spyOn(deploymentService, 'createDeployment').and.returnValue(Observable.of(<Deployment>{}));
    // when
    deploymentComponent.requestDeployment();
    // then
    expect(deploymentService.createDeployment).toHaveBeenCalledWith(deploymentRequest);
  }));

  it('should call the deploymentService with the right values on createDeployment', inject([DeploymentComponent, DeploymentService], (deploymentComponent: DeploymentComponent, deploymentService: DeploymentService) => {
    // given
    deploymentComponent.selectedAppserver = <Resource>{name: 'testServer'};
    deploymentComponent.selectedRelease = <Release>{id: 1, release: 'testRelease'};
    deploymentComponent.environments = [<Environment>{id: 2, name: 'A', selected: true}, <Environment>{id: 3, name: 'B'}];
    deploymentComponent.doExecuteShakedownTest = true;
    deploymentComponent.appsWithVersion = [<AppWithVersion>{applicationId: 4, applicationName: 'testApp', version: '1.2.3'}, <AppWithVersion>{applicationId: 5, applicationName: 'testAPP', version: '1.2.3.4'}];
    deploymentComponent.selectedResourceTag = <ResourceTag> {id: 5, tagDate: 1485378084103};
    deploymentComponent.transDeploymentParameters = [ <DeploymentParameter>{key:'atest', value:'foo'}, <DeploymentParameter>{key:'btest', value:'bar'} ];
    let deploymentRequest: DeploymentRequest = <DeploymentRequest>{ appServerName: 'testServer', environmentName: 'A',
      releaseName: 'testRelease', sendEmail: false, executeShakedownTest: deploymentComponent.doExecuteShakedownTest,
      neighbourhoodTest: false, requestOnly: false,  appsWithVersion: deploymentComponent.appsWithVersion,
      stateToDeploy: deploymentComponent.selectedResourceTag.tagDate, deploymentDate: null, deploymentParameters: deploymentComponent.transDeploymentParameters};
    spyOn(deploymentService, 'createDeployment').and.returnValue(Observable.of(<Deployment>{}));
    // when
    deploymentComponent.createDeployment();
    // then
    expect(deploymentService.createDeployment).toHaveBeenCalledWith(deploymentRequest);
  }));

});
