import { Component, OnInit, AfterViewInit } from '@angular/core';
import { Location } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import { AppState } from '../app.service';
import { ResourceService } from '../resource/resource.service';
import { ResourceTag } from '../resource/resource-tag';
import { Resource } from '../resource/resource';
import { Release } from '../resource/release';
import { Relation } from '../resource/relation';
import { DeploymentParameter } from './deployment-parameter';
import { DeploymentService } from './deployment.service';
import { EnvironmentService } from './environment.service';
import { Environment } from './environment';
import { DeploymentRequest } from './deployment-request';
import { AppWithVersion } from './app-with-version';
import { Subscription } from 'rxjs';
import * as _ from 'lodash';
import * as moment from 'moment';

declare var $: any;

@Component({
  selector: 'amw-deployment',
  templateUrl: './deployment.component.html',
  providers: [DeploymentService]
})
export class DeploymentComponent implements OnInit, AfterViewInit {

  // from url
  appserverName: string = '';
  releaseName: string = '';

  // these are valid for all (loaded ony once)
  appservers: Resource[] = [];
  environments: Environment[] = [];
  environmentGroups: string[] = [];
  deploymentParameters: DeploymentParameter[] = [];

  // per appserver/deployment request
  selectedAppserver: Resource = null;
  releases: Release[] = [];
  selectedRelease: Release = null;
  runtime: Relation = null;
  resourceTags: ResourceTag[] = [<ResourceTag> {label: 'HEAD'}];
  selectedResourceTag: ResourceTag = null;
  deploymentDate: string = '';
  appsWithVersion: AppWithVersion[] = [];
  transDeploymentParameter: DeploymentParameter = <DeploymentParameter> {};
  transDeploymentParameters: DeploymentParameter[] = [];

  simulate: boolean = false;
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
  }

  ngAfterViewInit(): void {
    $('.datepicker').datetimepicker({format: 'DD.MM.YYYY HH:mm'});
    // we dont need this right away
    this.loadDeploymentParameters();
  }

  initAppservers() {
    this.isLoading = true;
    this.resourceService
      .getByType('APPLICATIONSERVER').subscribe(
      /* happy path */ (r) => this.appservers = _.sortBy(_.uniqBy(r, 'id'), 'name'),
      /* error path */ (e) => this.errorMessage = e,
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

  onAddParam() {
    _.remove(this.transDeploymentParameters, { key: this.transDeploymentParameter.key });
    this.transDeploymentParameters.push(this.transDeploymentParameter);
    this.transDeploymentParameter = <DeploymentParameter> {};
  }

  onRemoveParam(deParam: DeploymentParameter) {
    _.pull(this.transDeploymentParameters, deParam);
  }

  isReadyForDeployment(): boolean {
    return (this.selectedRelease && _.filter(this.environments, 'selected').length > 0);
  }

  requestDeployment() {
    this.requestOnly = true;
    console.log('requestDeployment()');
    this.prepareDeployment();
  }

  createDeployment() {
    this.requestOnly = false;
    console.log('createDeployment()');
    this.prepareDeployment();
  }

  private setSelectedRelease(): Subscription {
    return this.resourceService.getMostRelevantRelease(this.selectedAppserver.id).subscribe(
      /* happy path */ (r) => this.selectedRelease = this.releases.find((release) => release.release === r.release),
      /* error path */ (e) => this.errorMessage = e,
      /* onComplete */ () => this.onChangeRelease());
  }

  private loadReleases(): Subscription {
    this.isLoading = true;
    return this.resourceService.getDeployableReleases(this.selectedAppserver.id).subscribe(
      /* happy path */ (r) => this.releases = r,
      /* error path */ (e) => this.errorMessage = e,
      /* onComplete */ () => this.setSelectedRelease());
  }

  private getRelatedForRelease() {
    this.isLoading = true;
    this.resourceService.getLatestForRelease(this.selectedAppserver.id, this.selectedRelease.id).subscribe(
      /* happy path */ (r) => this.bestForSelectedRelease = r,
      /* error path */ (e) => this.errorMessage = e,
      /* onComplete */ () => this.extractFromRelations());
  }

  private extractFromRelations() {
    this.runtime = _.filter(this.bestForSelectedRelease.relations, {type: 'RUNTIME'}).pop();
    this.appsWithoutVersion = _.filter(this.bestForSelectedRelease.relations, {type: 'APPLICATION'}).map((val) => val.relatedResourceName);
    this.resourceTags = this.resourceTags.concat(this.bestForSelectedRelease.resourceTags);
    this.appsWithVersion = [];
    this.getAppVersions();
  }

  private getAppVersions() {
    this.isLoading = true;
    this.resourceService.getAppsWithVersions(this.selectedAppserver.id, this.bestForSelectedRelease.id, _.filter(this.environments, 'selected').map((val) => val.id)).subscribe(
      /* happy path */ (r) => this.appsWithVersion = r,
      /* error path */ (e) => this.errorMessage = e,
      /* onComplete */ () => this.isLoading = false);
  }

  private resetVars() {
    this.selectedRelease = null;
    this.bestForSelectedRelease = null;
    this.selectedResourceTag = null;
    this.deploymentDate = null;
    this.resourceTags = [<ResourceTag> {label: 'HEAD'}];
    this.simulate = false;
    this.doSendEmail = false;
    this.doExecuteShakedownTest = false;
    this.doNeighbourhoodTest = false;
    this.appsWithVersion = [];
    this.appsWithoutVersion = [];
    this.transDeploymentParameter = <DeploymentParameter> {};
    this.transDeploymentParameters = [];
  }

  private prepareDeployment() {
    if (this.isReadyForDeployment()) {
      let environments: string[] = _.filter(this.environments, 'selected').map((val) => val.name);
      environments.forEach((environmentName) => this.createDeploymentRequest(environmentName));
    }
  }

  private createDeploymentRequest(environmentName: string) {
    let deploymentRequest: DeploymentRequest = <DeploymentRequest> {};
    deploymentRequest.appServerName = this.selectedAppserver.name;
    deploymentRequest.releaseName = this.selectedRelease.release;
    deploymentRequest.environmentName = environmentName;
    deploymentRequest.simulate = this.simulate;
    deploymentRequest.sendEmail = this.doSendEmail;
    deploymentRequest.executeShakedownTest = this.doExecuteShakedownTest;
    deploymentRequest.neighbourhoodTest = this.doNeighbourhoodTest;
    deploymentRequest.requestOnly = this.requestOnly;
    deploymentRequest.appsWithVersion = this.appsWithVersion;
    deploymentRequest.stateToDeploy = (this.selectedResourceTag && this.selectedResourceTag.tagDate) ? this.selectedResourceTag.tagDate : new Date().getTime();
    if (this.deploymentDate) {
      let dateTime = moment(this.deploymentDate, 'DD.MM.YYYY hh:mm');
      if (dateTime && dateTime.isValid()) {
        deploymentRequest.deploymentDate = dateTime.valueOf();
      }
    }
    if (this.transDeploymentParameters.length > 0) {
      deploymentRequest.deploymentParameters = this.transDeploymentParameters;
    }
    this.deploymentService.createDeployment(deploymentRequest).subscribe(
      /* happy path */ (r) => r, // => this.relations = r,
      /* error path */ (e) => this.errorMessage = e);
  }

  private initEnvironments() {
    this.isLoading = true;
    this.environmentService
      .getAll().subscribe(
      /* happy path */ (r) => this.environments = r,
      /* error path */ (e) => this.errorMessage = e,
      /* onComplete */ () => this.extractEnvironmentGroups());
  }

  private extractEnvironmentGroups() {
    this.environmentGroups = Array.from(new Set(this.environments.map((env) => env.parent)));
    this.isLoading = false;
  }

  private loadDeploymentParameters() {
    this.deploymentService
      .getAllDeploymentParameterKeys().subscribe(
      /* happy path */ (r) => this.deploymentParameters = r,
      /* error path */ (e) => this.errorMessage = e);
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
        /* happy path */ (r) => this.releases = r,
        /* error path */ (e) => this.errorMessage = e,
        /* onComplete */ () => this.setRelease());
      this.isLoading = false;
    }
  }

  // for url params only
  private setRelease() {
    if (this.releaseName) {
      console.log('pre-selected release is ' + this.releaseName);
      this.selectedRelease = this.releases.find((release) => release.release === this.releaseName);
      this.onChangeRelease();
    }
  }

  private goTo(destination: string) {
    this.location.go('deployment/' + destination);
  }

}
