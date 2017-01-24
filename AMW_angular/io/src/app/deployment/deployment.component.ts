import { Component, OnInit } from '@angular/core';
import { Location } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import { AppState } from '../app.service';
import { ResourceService } from '../resource/resource.service';
import { Resource } from '../resource/resource';
import { Release } from '../resource/release';
import { Relation } from '../resource/relation';
import { DeploymentParameter } from './deployment-parameter';
import { DeploymentService } from './deployment.service';
import { EnvironmentService } from './environment.service';
import { Environment } from './environment';
import { DeploymentRequest } from './deployment-request';
import { AppWithVersion } from './app-with-version';
import * as _ from 'lodash';
import { Subscription } from 'rxjs';


@Component({
  selector: 'amw-deployment',
  templateUrl: './deployment.component.html',
  providers: [DeploymentService]
})
export class DeploymentComponent implements OnInit {

  // from url
  appserverName: string = '';
  releaseName: string = '';

  // these are valid for all (loaded ony once)
  appservers: Resource[] = [];
  environments: Environment[] = [];
  environmentGroups: string[] = [];
  deploymentParameter: DeploymentParameter[] = [];

  // per appserver/deployment request
  selectedAppserver: Resource = null;
  releases: Release[] = [];
  selectedRelease: Release = null;
  runtime: Relation = null;
  appsWithVersion: AppWithVersion[] = [];
  deploymentRequests: DeploymentRequest[] = [];

  requestOnly: boolean = false;
  doSendEmail: boolean = false;
  doExecuteShakedownTest: boolean = false;
  // may only be enabled if above is true
  doNeighbourhoodTest: boolean = false;

  bestForSelectedRelease: Release = null;
  appsWithoutVersion: string[] = [];

  errorMessage: string = '';
  isLoading: boolean = false;

  constructor(private resourceService: ResourceService,
              private environmentService: EnvironmentService,
              private deploymentService: DeploymentService,
              private activatedRoute: ActivatedRoute,
              private location: Location,
              private appState: AppState) {
  }

  ngOnInit(): void {

    this.appState.set('navTitle', 'Deployments');
    this.appState.set('pageTitle', 'Create new deployment');

    this.activatedRoute.params.subscribe(
      (param: any) => {
        this.appserverName = param['appserverName'];
        console.log('appserverName from param: ' + this.appserverName);
        this.releaseName = param['releaseName'];
        console.log('releaseName from param: ' + this.releaseName);
      });

    console.log('hello `Deployment` component');

    this.initAppservers();
    this.initEnvironments();
    // we dont need this right away
    this.loadDeploymentParameters();
  }

  initAppservers() {
    this.isLoading = true;
    this.resourceService
      .getByType('APPLICATIONSERVER').subscribe(
      /* happy path */ r => this.appservers = _.sortBy(_.uniqBy(r, 'id'), 'name'),
      /* error path */ e => this.errorMessage = e,
      /* onComplete */ () => this.setPreselected());
  }

  onChangeAppserver() {
    this.resetVars();
    this.loadReleases();
  }

  onChangeRelease() {
    console.log('selected release is ' + this.selectedRelease.release);
    if (!this.selectedRelease) {
      this.selectedRelease = this.releases[0];
    }
    this.getRelatedForRelease();
    this.goTo(this.selectedAppserver.name + '/' + this.selectedRelease.release);
  }

  onChangeEnvironment() {
    this.getAppVersions();
  }

  isReadyForDeployment(): boolean {
    return (this.selectedRelease && _.filter(this.environments, 'selected').length > 0);
  }

  requestDeployment() {
    this.requestOnly = false;
    console.log('requestDeployment()');
    this.prepareDeployment();
  }

  createDeployment() {
    this.requestOnly = true;
    console.log('createDeployment()');
    this.prepareDeployment();
  }

  private setSelectedRelease(): Subscription {
    return this.resourceService.getMostRelevantRelease(this.selectedAppserver.id).subscribe(
      /* happy path */ r => this.selectedRelease = this.releases.find(release => release.release === r.release),
      /* error path */ e => this.errorMessage = e,
      /* onComplete */ () => this.onChangeRelease());
  }

  private loadReleases(): Subscription {
    console.log('loading releases for ' + this.selectedAppserver.name);
    this.isLoading = true;
    return this.resourceService.getDeployableReleases(this.selectedAppserver.id).subscribe(
      /* happy path */ r => this.releases = r,
      /* error path */ e => this.errorMessage = e,
      /* onComplete */ () => this.setSelectedRelease());
  }

  private getRelatedForRelease() {
    console.log('getRelatedForRelease ' + this.selectedRelease.release);
    this.isLoading = true;
    this.resourceService.getLatestForRelease(this.selectedAppserver.id, this.selectedRelease.id).subscribe(
      /* happy path */ r => this.bestForSelectedRelease = r,
      /* error path */ e => this.errorMessage = e,
      /* onComplete */ () => this.extractFromRelations());
  }

  private extractFromRelations() {
     this.runtime = _.filter(this.bestForSelectedRelease.relations, {'type': 'RUNTIME'}).pop();
     this.appsWithoutVersion = _.filter(this.bestForSelectedRelease.relations, {'type': 'APPLICATION'}).map(val => val.relatedResourceName);
     console.log('got appsWithoutVersion ' + this.appsWithoutVersion);
     this.appsWithVersion = [];
     this.getAppVersions();
  }

  private getAppVersions() {
    console.log('getAppVersions');
    if (!this.bestForSelectedRelease) {
      console.log('!! bestForSelectedRelease is undefined !!');
    }
    this.isLoading = true;
    this.resourceService.getAppsWithVersions(this.selectedAppserver.id, this.bestForSelectedRelease.id, _.filter(this.environments, 'selected').map(val => val.id)).subscribe(
      /* happy path */ r => this.appsWithVersion = r,
      /* error path */ e => this.errorMessage = e,
      /* onComplete */ () => this.isLoading = false);
  }

  private resetVars() {
    this.selectedRelease = null;
    this.bestForSelectedRelease = null;
    this.doSendEmail = false;
    this.doExecuteShakedownTest = false;
    this.doNeighbourhoodTest = false;
    this.appsWithVersion = [];
    this.appsWithoutVersion = [];
/*    this.environments.forEach(function (item) {
      item.selected = false
    });*/
  }

  private prepareDeployment() {
    if (this.isReadyForDeployment()) {
      let environments: string[] = _.filter(this.environments, 'selected').map(val => val.name);
      environments.forEach(function (environmentName) {
        let deploymentRequest: DeploymentRequest = <DeploymentRequest>{};
        deploymentRequest.appServerName = this.selectedAppserver.name;
        deploymentRequest.releaseName = this.selectedRelease.release;
        deploymentRequest.environmentName = environmentName;
        deploymentRequest.sendEmail = this.doSendEmail;
        deploymentRequest.executeShakedownTest = this.doExecuteShakedownTest;
        deploymentRequest.neighbourhoodTest = this.doNeighbourhoodTest;
        deploymentRequest.requestOnly = this.requestOnly;
        deploymentRequest.appsWithVersion = this.appsWithVersion;
        // TODO Deploymentparameter
        console.log(deploymentRequest);

        this.deploymentService.createDeployment(deploymentRequest).subscribe(
          /* happy path */ r => r, // => this.relations = r,
          /* error path */ e => this.errorMessage = e);

        this.deploymentRequests.push(deploymentRequest);

      }, this);
    }
  }

  private initEnvironments() {
    this.isLoading = true;
    this.environmentService
      .getAll().subscribe(
      /* happy path */ r => this.environments = r,
      /* error path */ e => this.errorMessage = e,
      /* onComplete */ () => this.extractEnvironmentGroups());
  }

  private extractEnvironmentGroups() {
    this.environmentGroups = Array.from(new Set(this.environments.map(env => env.parent)));
    this.isLoading = false;
  }

  private loadDeploymentParameters() {
    this.deploymentService
      .getAllDeploymentParameterKeys().subscribe(
      /* happy path */ r => this.deploymentParameter = r,
      /* error path */ e => this.errorMessage = e);
  }

  // for url params only
  private setPreselected() {
    if (this.appserverName) {
      console.log('pre-selected server is ' + this.appserverName);
      for (let i = 0; i < this.appservers.length; i++) {
        if (this.appservers[i].name === this.appserverName) {
          this.selectedAppserver = this.appservers[i];
          break;
        }
      }
      this.resourceService.getDeployableReleases(this.selectedAppserver.id).subscribe(
        /* happy path */ r => this.releases = r,
        /* error path */ e => this.errorMessage = e,
        /* onComplete */ () => this.setRelease());
      this.isLoading = false;
    }
  }

  // for url params only
  private setRelease() {
    if (this.releaseName) {
      console.log('pre-selected release is ' + this.releaseName);
      this.selectedRelease = this.releases.find(release => release.release === this.releaseName);
      this.onChangeRelease();
    }
  }

  private goTo(destination: string) {
    this.location.go('deployment/' + destination);
  }

}
