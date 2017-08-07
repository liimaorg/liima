import { Component, OnInit, AfterViewInit } from '@angular/core';
import { Location } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import { AppState } from '../app.service';
import { ResourceService } from '../resource/resource.service';
import { ResourceTag } from '../resource/resource-tag';
import { Resource } from '../resource/resource';
import { Release } from '../resource/release';
import { Relation } from '../resource/relation';
import { Deployment } from './deployment';
import { DeploymentParameter } from './deployment-parameter';
import { DeploymentService } from './deployment.service';
import { EnvironmentService } from './environment.service';
import { Environment } from './environment';
import { DeploymentRequest } from './deployment-request';
import { AppWithVersion } from './app-with-version';
import { Subscription } from 'rxjs';
import { Datetimepicker } from 'eonasdan-bootstrap-datetimepicker';
import * as _ from 'lodash';
import * as moment from 'moment';

declare var $: any;

@Component({
  selector: 'amw-deployment',
  templateUrl: './deployment.component.html'
})
export class DeploymentComponent implements OnInit, AfterViewInit {

  // from url
  appserverName: string = '';
  releaseName: string = '';
  // redeploy only
  deploymentId: number;

  // these are valid for all (loaded ony once)
  appservers: Resource[] = [];
  environments: Environment[] = [];
  groupedEnvironments: { [key: string]: Environment[] } = {};
  deploymentParameters: DeploymentParameter[] = [];
  defaultResourceTag: ResourceTag = <ResourceTag> {label: 'HEAD'};
  isRedeployment: boolean = false;

  // per appserver/deployment request
  selectedAppserver: Resource = null;
  releases: Release[] = [];
  selectedRelease: Release = null;
  runtime: Relation = null;
  resourceTags: ResourceTag[] = [this.defaultResourceTag];
  selectedResourceTag: ResourceTag = this.defaultResourceTag;
  deploymentDate: string = '';
  appsWithVersion: AppWithVersion[] = [];
  transDeploymentParameter: DeploymentParameter = <DeploymentParameter> {};
  transDeploymentParameters: DeploymentParameter[] = [];
  deploymentResponse: any = {};
  hasPermissionShakedownTest: boolean = false;
  hasPermissionToDeploy: boolean = false;

  // redeploy only
  selectedDeployment: Deployment = <Deployment> {};
  redeploymentAppserverDisplayName: string = '';

  simulate: boolean = false;
  requestOnly: boolean = false;
  doSendEmail: boolean = false;
  doExecuteShakedownTest: boolean = false;
  // may only be enabled if above is true
  doNeighbourhoodTest: boolean = false;

  bestForSelectedRelease: Release = null;

  errorMessage: string = '';
  successMessage: string = '';
  isLoading: boolean = false;

  constructor(private resourceService: ResourceService,
              private environmentService: EnvironmentService,
              private deploymentService: DeploymentService,
              private activatedRoute: ActivatedRoute,
              private location: Location,
              public appState: AppState) {
  }

  ngOnInit() {

    this.appState.set('navShow', false);
    this.appState.set('navTitle', 'Deployments');

    this.activatedRoute.params.subscribe(
      (param: any) => {
        this.appserverName = param['appserverName'];
        this.releaseName = param['releaseName'];
        this.deploymentId = param['deploymentId'];
      });

    console.log('hello `Deployment` component');

    // a deploymentId MUST be numeric..
    if (this.deploymentId && !isNaN(this.deploymentId)) {
      this.appState.set('pageTitle', 'Redeploy');
      this.isRedeployment = true;
      this.getDeployment();
    } else {
      // ..or it's rather an appserverName (we got no type safety on runtime)
      if (this.deploymentId) {
        this.appserverName = this.deploymentId.toString();
        delete this.deploymentId;
      }
      this.appState.set('pageTitle', 'Create new deployment');
      this.initAppservers();
    }
    this.initEnvironments();
  }

  ngAfterViewInit() {
    $('.datepicker').datetimepicker({format: 'DD.MM.YYYY HH:mm'});
    // we dont need this right away
    this.loadDeploymentParameters();
  }

  initAppservers() {
    this.isLoading = true;
    this.resourceService
      .getByType('APPLICATIONSERVER').subscribe(
      /* happy path */ (r) => this.appservers = r.sort(function(a, b){return a.name.localeCompare(b.name, undefined, { sensitivity: 'base' }); }),
      /* error path */ (e) => this.errorMessage = e,
      /* onComplete */ () => this.setPreselected());
  }

  onChangeAppserver() {
    this.resetVars();
    this.loadReleases();
    this.canCreateShakedownTest();
    this.canDeploy();
  }

  onChangeRelease() {
    if (!this.selectedRelease) {
      this.selectedRelease = this.releases[0];
    }
    console.log('selected release is ' + this.selectedRelease.release);
    this.getRelatedForRelease();
    this.goTo(this.selectedAppserver.name + '/' + this.selectedRelease.release);
  }

  onChangeEnvironment() {
    if (!this.isRedeployment) {
      this.getAppVersions();
    }
    this.canDeploy();
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
    return (this.selectedRelease  && this.appsWithVersion.length > 0 && _.filter(this.environments, 'selected').length > 0);
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

  getEnvironmentGroups() {
    return Object.keys(this.groupedEnvironments);
  }

  private getDeployment() {
    this.isLoading = true;
    return this.deploymentService.get(this.deploymentId).subscribe(
      /* happy path */ (r) => this.selectedDeployment = r,
      /* error path */ (e) => this.errorMessage = e,
      /* onComplete */ () => this.initRedeploymentValues());
  }

  private initRedeploymentValues() {
    this.isLoading = false;
    this.composeRedeploymentAppserverDisplayName();
    this.setPreSelectedEnvironment();
    this.transDeploymentParameters = this.selectedDeployment.deploymentParameters;
    this.appsWithVersion = this.selectedDeployment.appsWithVersion;
    this.selectedRelease = <Release> { release: this.selectedDeployment.releaseName };
    this.selectedAppserver = <Resource> { name: this.selectedDeployment.appServerName };
  }

  private composeRedeploymentAppserverDisplayName() {
    this.redeploymentAppserverDisplayName = this.redeploymentAppserverDisplayName.concat('<h5>', this.selectedDeployment.appServerName, ' (', this.selectedDeployment.releaseName, ')', '</h5>');
    this.selectedDeployment.appsWithVersion.forEach((appWithVersion) => { this.redeploymentAppserverDisplayName =
      this.redeploymentAppserverDisplayName.concat('<h6>', appWithVersion.applicationName, ' (', appWithVersion.version, ')', '</h6>');
    });
  }

  private setPreSelectedEnvironment() {
    let env = _.find(this.environments, {name: this.selectedDeployment.environmentName});
    if (env) { env.selected = true; }
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
    this.resourceTags = this.resourceTags.concat(this.bestForSelectedRelease.resourceTags);
    this.appsWithVersion = [];
    this.getAppVersions();
  }

  private getAppVersions() {
    this.isLoading = true;
    this.resourceService.getAppsWithVersions(this.selectedAppserver.id, this.selectedRelease.id, _.filter(this.environments, 'selected').map((val) => val.id)).subscribe(
      /* happy path */ (r) => this.appsWithVersion = r,
      /* error path */ (e) => this.errorMessage = e,
      /* onComplete */ () => this.isLoading = false);
  }

  private resetVars() {
    this.errorMessage = '';
    this.successMessage = '';
    this.hasPermissionShakedownTest = false;
    this.selectedRelease = null;
    this.bestForSelectedRelease = null;
    this.resourceTags = [this.defaultResourceTag];
    this.selectedResourceTag = this.defaultResourceTag;
    this.deploymentDate = null;
    this.simulate = false;
    this.doSendEmail = false;
    this.doExecuteShakedownTest = false;
    this.doNeighbourhoodTest = false;
    this.appsWithVersion = [];
    this.transDeploymentParameter = <DeploymentParameter> {};
    this.transDeploymentParameters = [];
  }

  private canCreateShakedownTest() {
    this.resourceService.canCreateShakedownTest(this.selectedAppserver.id).subscribe(
      /* happy path */ (r) => this.hasPermissionShakedownTest = r,
      /* error path */ (e) => this.errorMessage = e);
  }

  private canDeploy() {
    if (this.selectedAppserver != null) {
      this.hasPermissionToDeploy = false;
      let contextIds: number[] = _.filter(this.environments, 'selected').map((val) => val.id);
      if (contextIds.length > 0) {
        this.deploymentService.canDeploy(this.selectedAppserver.id, contextIds).subscribe(
          /* happy path */ (r) => this.hasPermissionToDeploy = r,
          /* error path */ (e) => this.errorMessage = e);
      }
    }
  }

  private prepareDeployment() {
    if (this.isReadyForDeployment()) {
      let contextIds: number[] = _.filter(this.environments, 'selected').map((val) => val.id);
      this.createDeploymentRequest(contextIds);
    }
  }

  private createDeploymentRequest(contextIds: number[]) {
    this.errorMessage = '';
    this.successMessage = '';
    let deploymentRequest: DeploymentRequest = <DeploymentRequest> {};
    deploymentRequest.appServerName = this.selectedAppserver.name;
    deploymentRequest.releaseName = this.selectedRelease.release;
    deploymentRequest.contextIds = contextIds;
    deploymentRequest.simulate = this.simulate;
    deploymentRequest.sendEmail = this.doSendEmail;
    deploymentRequest.executeShakedownTest = this.doExecuteShakedownTest;
    deploymentRequest.neighbourhoodTest = this.doNeighbourhoodTest;
    deploymentRequest.requestOnly = this.requestOnly;
    deploymentRequest.appsWithVersion = this.appsWithVersion;
    if (!this.isRedeployment) {
      deploymentRequest.stateToDeploy = (this.selectedResourceTag && this.selectedResourceTag.tagDate) ? this.selectedResourceTag.tagDate : new Date().getTime();
    }
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
      /* happy path */ (r) => this.deploymentResponse = r,
      /* error path */ (e) => this.errorMessage = e,
      /* onComplete */ () => this.composeSuccessMessage());
  }

  private composeSuccessMessage() {
    let link: string = '<a href="/AMW_web/pages/deploy.xhtml?tracking_id=' + this.deploymentResponse.trackingId + '">Tracking Id ' + this.deploymentResponse.trackingId + '</a>';
    this.successMessage = 'Deployment created: <strong>' + link + '</strong>';
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
    this.environments.forEach((environment) => {
      if (!this.groupedEnvironments[environment['parent']]) {
        this.groupedEnvironments[environment['parent']] = [];
      }
      this.groupedEnvironments[environment['parent']].push(environment);
    });
    this.isLoading = false;
  }

  private loadDeploymentParameters() {
    this.deploymentService
      .getAllDeploymentParameterKeys().subscribe(
      /* happy path */ (r) => this.deploymentParameters = r.sort(function(a, b){return a.key.localeCompare(b.key, undefined, { sensitivity: 'base' }); }),
      /* error path */ (e) => this.errorMessage = e);
  }

  // for url params only
  private setPreselected() {
    if (this.appserverName) {
      console.log('pre-selected server is ' + this.appserverName);
      this.selectedAppserver = _.find(this.appservers, {name: this.appserverName});
      if (this.selectedAppserver) {
        this.resourceService.getDeployableReleases(this.selectedAppserver.id).subscribe(
          /* happy path */ (r) => this.releases = r,
          /* error path */ (e) => this.errorMessage = e,
          /* onComplete */ () => this.setRelease());
      }
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
    this.location.go('/deployment/' + destination);
  }

}
