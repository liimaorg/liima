import { Component, OnInit } from '@angular/core';
import { Location } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import { ResourceService } from '../resource/resource.service';
import { Resource } from '../resource/resource';
import { Release } from '../resource/release';
import { Relation } from '../resource/relation';
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

  selectedAppserver: Resource = null;
  appservers: Resource[] = [];
  releases: Release[] = [];
  selectedRelease: Release = null;
  runtime: Relation = null;
  environments: Environment[] = [];
  environmentGroups: string[] = [];

  appsWithVersion: AppWithVersion[] = [];

  deploymentRequests: DeploymentRequest[] = [];

  requestOnly: boolean = false;
  doSendEmail: boolean = false;
  doExecuteShakedownTest: boolean = false;
  // may only be enabled if above is true
  doNeighbourhoodTest: boolean = false;

  titleLabel: string = 'Create new deployment';
  errorMessage: string = '';
  isLoading: boolean = false;

  constructor(private resourceService: ResourceService,
              private environmentService: EnvironmentService,
              private deploymentService: DeploymentService,
              private activatedRoute: ActivatedRoute,
              private location: Location) {
  }

  ngOnInit(): void {

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
  }

  onChangeAppserver() {
    this.resetVars();
    this.loadReleases();
  }

  private loadReleases(): Subscription {
    console.log('loading releases for ' + this.selectedAppserver.name);
    this.isLoading = true;
    return this.resourceService.getDeployableReleases(this.selectedAppserver.name).subscribe(
      /* happy path */ r => this.releases = r,
      /* error path */ e => this.errorMessage = e,
      /* onComplete */ () => this.onChangeRelease());
  }

  onChangeRelease() {
    if (!this.selectedRelease) {
      this.selectedRelease = this.releases[0];
    }
    this.getRuntime();
    this.getAppVersion();
    this.goTo(this.selectedAppserver.name + '/' + this.selectedRelease.release);
  }

  onChangeEnvironment() {
    this.getAppVersion();
  }

  private getAppVersion() {
    this.isLoading = true;
    this.resourceService.getAppversion(this.selectedAppserver.name, this.selectedRelease.release, _.filter(this.environments, 'selected').map(val => val.id)).subscribe(
      /* happy path */ r => this.appsWithVersion = r,
      /* error path */ e => this.errorMessage = e,
      /* onComplete */ () => this.isLoading = false);
  }

  private getRuntime() {
    this.isLoading = true;
    this.resourceService.getRuntime(this.selectedAppserver.name, this.selectedRelease.release).subscribe(
      /* happy path */ r => this.runtime = r.pop(),
      /* error path */ e => this.errorMessage = e,
      /* onComplete */ () => this.isLoading = false);
  }

  private resetVars() {
    this.selectedRelease = null;
    this.doSendEmail = false;
    this.doExecuteShakedownTest = false;
    this.doNeighbourhoodTest = false;
    this.appsWithVersion = [];
    this.environments.forEach(function (item) {
      item.selected = false
    });
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

  initAppservers() {
    this.isLoading = true;
    this.resourceService
      .getByType('APPLICATIONSERVER').subscribe(
      /* happy path */ r => this.appservers = _.sortBy(_.uniqBy(r, 'id'), 'name'),
      /* error path */ e => this.errorMessage = e,
      /* onComplete */ () => this.setPreselected());
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
    }
    this.resourceService.getDeployableReleases(this.appserverName).subscribe(
      /* happy path */ r => this.releases = r,
      /* error path */ e => this.errorMessage = e,
      /* onComplete */ () => this.setRelease());
    this.isLoading = false;
  }

  // for url params only
  private setRelease() {
    if (this.releaseName) {
      console.log('pre-selected release is ' + this.releaseName);
      for (let i = 0; i < this.releases.length; i++) {
        if (this.releases[i].release === this.releaseName) {
          this.selectedRelease = this.releases[i];
          this.onChangeRelease();
          return;
        }
      }
    }
  }

  hasRelease(releaseName: string): boolean {
    if (this.releases) {
      for (let i = 0; i < this.releases.length; i++) {
        if (this.releases[i].release === releaseName) {
          return true;
        }
      }
    }
    return false;
  }

  private goTo(destination: string) {
    this.location.go('deployment/' + destination);
  }

}
