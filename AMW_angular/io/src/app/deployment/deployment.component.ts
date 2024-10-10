import { Component, OnInit, AfterViewInit } from '@angular/core';
import { Location } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
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
import * as _ from 'lodash';
import { DateTimeModel } from '../shared/date-time-picker/date-time.model';
import { IconComponent } from '../shared/icon/icon.component';
import { DateTimePickerComponent } from '../shared/date-time-picker/date-time-picker.component';
import { FormsModule } from '@angular/forms';
import { NgSelectModule } from '@ng-select/ng-select';
import { NotificationComponent } from '../shared/elements/notification/notification.component';
import { LoadingIndicatorComponent } from '../shared/elements/loading-indicator.component';
import { PageComponent } from '../layout/page/page.component';

@Component({
  selector: 'app-deployment',
  templateUrl: './deployment.component.html',
  standalone: true,
  imports: [
    LoadingIndicatorComponent,
    NotificationComponent,
    NgSelectModule,
    FormsModule,
    DateTimePickerComponent,
    IconComponent,
    PageComponent,
  ],
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
  defaultResourceTag: ResourceTag = { label: 'HEAD' } as ResourceTag;
  isRedeployment: boolean = false;

  // per appserver/deployment request
  selectedAppserver: Resource = null;
  releases: Release[] = [];
  selectedRelease: Release = null;
  runtime: Relation = null;
  resourceTags: ResourceTag[] = [this.defaultResourceTag];
  selectedResourceTag: ResourceTag = this.defaultResourceTag;
  deploymentDate: DateTimeModel = null;
  appsWithVersion: AppWithVersion[] = [];
  transDeploymentParameter: DeploymentParameter = {} as DeploymentParameter;
  transDeploymentParameters: DeploymentParameter[] = [];
  deploymentResponse: any = {};
  hasPermissionToDeploy: boolean = false;
  hasPermissionToRequestDeployment: boolean = false;

  // redeploy only
  selectedDeployment: Deployment = {} as Deployment;
  redeploymentAppserverDisplayName: string = '';
  appsWithVersionForRedeployment: AppWithVersion[] = [];

  simulate: boolean = false;
  requestOnly: boolean = false;
  doSendEmail: boolean = false;

  bestForSelectedRelease: Release = null;

  errorMessage: string = '';
  successMessage: string = '';
  isLoading: boolean = false;
  isDeploymentBlocked: boolean = false;

  constructor(
    private resourceService: ResourceService,
    private environmentService: EnvironmentService,
    private deploymentService: DeploymentService,
    private activatedRoute: ActivatedRoute,
    private location: Location,
  ) {}

  ngOnInit() {
    this.initEnvironments();
    this.activatedRoute.params.subscribe((param: any) => {
      this.appserverName = param['appserverName'];
      this.releaseName = param['releaseName'];
      this.deploymentId = param['deploymentId'];

      if (this.deploymentId && Number(this.deploymentId)) {
        this.prepareRedeploy();
      } else {
        this.prepareNewDeployment();
      }
    });
  }

  ngAfterViewInit() {
    // we don't need this right away
    this.loadDeploymentParameters();
  }

  initAppservers() {
    this.isLoading = true;
    this.resourceService.getByType('APPLICATIONSERVER').subscribe({
      next: (r) =>
        (this.appservers = r.sort(function (a, b) {
          return a.name.localeCompare(b.name, undefined, {
            sensitivity: 'base',
          });
        })),
      error: (e) => (this.errorMessage = e),
      complete: () => {
        this.setPreselected();
        this.isLoading = false;
      },
    });
  }

  onChangeAppserver() {
    this.resetVars();
    this.loadReleases();
    this.canDeploy();
  }

  onChangeRelease() {
    if (!this.selectedRelease) {
      this.selectedRelease = this.releases[0];
    }
    this.getRelatedForRelease();
    this.goTo(this.selectedAppserver.name + '/' + this.selectedRelease.release);
  }

  onChangeEnvironment() {
    if (!this.isRedeployment) {
      this.getAppVersions();
    } else {
      this.verifyRedeployment();
    }
    this.canDeploy();
  }

  onAddParam() {
    _.remove(this.transDeploymentParameters, {
      key: this.transDeploymentParameter.key,
    });
    this.transDeploymentParameters.push(this.transDeploymentParameter);
    this.transDeploymentParameter = {} as DeploymentParameter;
  }

  onRemoveParam(deParam: DeploymentParameter) {
    _.pull(this.transDeploymentParameters, deParam);
  }

  isReadyForDeployment(): boolean {
    return (
      !this.isDeploymentBlocked &&
      this.selectedRelease &&
      this.appsWithVersion.length > 0 &&
      _.filter(this.environments, 'selected').length > 0
    );
  }

  requestDeployment() {
    this.requestOnly = true;
    this.prepareDeployment();
  }

  createDeployment() {
    this.requestOnly = false;
    this.prepareDeployment();
  }

  getEnvironmentGroups() {
    return Object.keys(this.groupedEnvironments);
  }

  private getDeployment(): Subscription {
    return this.deploymentService.get(this.deploymentId).subscribe({
      next: (r) => (this.selectedDeployment = r),
      error: (e) => (this.errorMessage = e),
      complete: () => this.initRedeploymentValues(),
    });
  }

  private initRedeploymentValues() {
    this.isLoading = false;
    this.composeRedeploymentAppserverDisplayName();
    this.transDeploymentParameters = this.selectedDeployment.deploymentParameters;
    this.appsWithVersion = this.selectedDeployment.appsWithVersion;
    this.selectedAppserver = {
      id: this.selectedDeployment.appServerId,
      name: this.selectedDeployment.appServerName,
    } as Resource;
    this.loadReleases();
    this.setPreSelectedEnvironment();
    this.canDeploy();
  }

  private composeRedeploymentAppserverDisplayName() {
    this.redeploymentAppserverDisplayName = this.redeploymentAppserverDisplayName.concat(
      '<h5>',
      this.selectedDeployment.appServerName,
      ' (',
      this.selectedDeployment.releaseName,
      ')',
      '</h5>',
    );
    this.selectedDeployment.appsWithVersion.forEach((appWithVersion) => {
      this.redeploymentAppserverDisplayName = this.redeploymentAppserverDisplayName.concat(
        '<h6>',
        appWithVersion.applicationName,
        ' (',
        appWithVersion.version,
        ')',
        '</h6>',
      );
    });
  }

  private setPreSelectedEnvironment() {
    const env = _.find(this.environments, {
      name: this.selectedDeployment.environmentName,
    });
    if (env) {
      env.selected = true;
    }
  }

  private setSelectedRelease(): Subscription {
    return this.resourceService.getMostRelevantRelease(this.selectedAppserver.id).subscribe({
      next: (r) => (this.selectedRelease = this.releases.find((release) => release.release === r.release)),
      error: (e) => (this.errorMessage = e),
      complete: () => this.onChangeRelease(),
    });
  }

  private setSelectedReleaseForRedeployment() {
    this.selectedRelease = this.releases.find((release) => release.release === this.selectedDeployment.releaseName);
    // will perform verifyRedeployment()
    this.getAppVersions();
  }

  private loadReleases(): Subscription {
    return this.resourceService.getDeployableReleases(this.selectedAppserver.id).subscribe({
      next: (r) => (this.releases = r),
      error: (e) => (this.errorMessage = e),
      complete: () => (this.isRedeployment ? this.setSelectedReleaseForRedeployment() : this.setSelectedRelease()),
    });
  }

  private getRelatedForRelease() {
    this.resourceService.getLatestForRelease(this.selectedAppserver.id, this.selectedRelease.id).subscribe({
      next: (r) => (this.bestForSelectedRelease = r),
      error: (e) => (this.errorMessage = e),
      complete: () => this.extractFromRelations(),
    });
  }

  private extractFromRelations() {
    this.runtime = _.filter(this.bestForSelectedRelease.relations, {
      type: 'RUNTIME',
    }).pop();
    this.resourceTags = this.resourceTags.concat(this.bestForSelectedRelease.resourceTags);
    this.appsWithVersion = [];
    this.getAppVersions();
  }

  private getAppVersions() {
    this.resourceService
      .getAppsWithVersions(
        this.selectedAppserver.id,
        this.selectedRelease.id,
        _.filter(this.environments, 'selected').map((val: Environment) => val.id),
      )
      .subscribe({
        next: (r) => (this.isRedeployment ? (this.appsWithVersionForRedeployment = r) : (this.appsWithVersion = r)),
        error: (e) => (this.errorMessage = e),
        complete: () => (this.isRedeployment ? this.verifyRedeployment() : ''),
      });
  }

  private resetVars() {
    this.errorMessage = '';
    this.successMessage = '';
    this.isDeploymentBlocked = false;
    this.selectedRelease = null;
    this.bestForSelectedRelease = null;
    this.resourceTags = [this.defaultResourceTag];
    this.selectedResourceTag = this.defaultResourceTag;
    this.deploymentDate = null;
    this.simulate = false;
    this.doSendEmail = false;
    this.appsWithVersion = [];
    this.transDeploymentParameter = {} as DeploymentParameter;
    this.transDeploymentParameters = [];
  }

  private canDeploy() {
    if (this.selectedAppserver != null) {
      this.hasPermissionToDeploy = false;
      const contextIds: number[] = _.filter(this.environments, 'selected').map((val: Environment) => val.id);
      if (contextIds.length > 0) {
        this.deploymentService.canDeploy(this.selectedAppserver.id, contextIds).subscribe({
          next: (r) => (this.hasPermissionToDeploy = r),
          error: (e) => (this.errorMessage = e),
          complete: () => this.canRequestDeployment(contextIds),
        });
      }
    }
  }

  private canRequestDeployment(contextIds: number[]) {
    if (this.selectedAppserver != null) {
      this.hasPermissionToRequestDeployment = false;
      if (contextIds.length > 0) {
        this.deploymentService.canRequestDeployment(this.selectedAppserver.id, contextIds).subscribe({
          next: (r) => (this.hasPermissionToRequestDeployment = r),
          error: (e) => (this.errorMessage = e),
        });
      }
    }
  }

  private verifyRedeployment() {
    this.errorMessage = '';
    this.isDeploymentBlocked = false;
    this.appsWithVersion.forEach((originApp: AppWithVersion) => {
      const actualApp: AppWithVersion = _.find(this.appsWithVersionForRedeployment, [
        'applicationName',
        originApp.applicationName,
      ]);
      if (!this.isDeploymentBlocked && !actualApp) {
        this.errorMessage = 'Application <strong>' + originApp.applicationName + '</strong> does not exist anymore';
        this.isDeploymentBlocked = true;
      }
    });
  }

  private prepareDeployment() {
    if (this.isReadyForDeployment()) {
      const contextIds: number[] = _.filter(this.environments, 'selected').map((val: Environment) => val.id);
      this.createDeploymentRequest(contextIds);
    }
  }

  private createDeploymentRequest(contextIds: number[]) {
    this.isLoading = true;
    this.errorMessage = '';
    this.successMessage = '';
    const deploymentRequest: DeploymentRequest = {} as DeploymentRequest;
    deploymentRequest.appServerName = this.selectedAppserver.name;
    deploymentRequest.releaseName = this.selectedRelease.release;
    deploymentRequest.contextIds = contextIds;
    deploymentRequest.simulate = this.simulate;
    deploymentRequest.sendEmail = this.doSendEmail;
    deploymentRequest.requestOnly = this.requestOnly;
    deploymentRequest.appsWithVersion = this.appsWithVersion;
    if (!this.isRedeployment) {
      deploymentRequest.stateToDeploy =
        this.selectedResourceTag && this.selectedResourceTag.tagDate
          ? this.selectedResourceTag.tagDate
          : new Date().getTime();
    }
    if (this.deploymentDate) {
      deploymentRequest.deploymentDate = this.deploymentDate.toEpoch();
    }
    if (this.transDeploymentParameters.length > 0) {
      deploymentRequest.deploymentParameters = this.transDeploymentParameters;
    }
    this.deploymentService.createDeployment(deploymentRequest).subscribe({
      next: (r) => (this.deploymentResponse = r),
      error: (e) => (this.errorMessage = e),
      complete: () => {
        this.isLoading = false;
        this.composeSuccessMessage();
      },
    });
  }

  private composeSuccessMessage() {
    const link = `<a href="#/deployments?filters=[{%22name%22:%22Tracking%20Id%22,%22val%22:%22${this.deploymentResponse.trackingId}%22}]">Tracking Id ${this.deploymentResponse.trackingId}</a>`;
    this.successMessage = 'Deployment created: <strong>' + link + '</strong>';
  }

  private initEnvironments() {
    this.isLoading = true;
    this.environmentService.getAll().subscribe({
      next: (r) => {
        this.environments = r;
        this.extractEnvironmentGroups();
      },
      error: (e) => (this.errorMessage = e),
      complete: () => (this.isLoading = false),
    });
  }

  private extractEnvironmentGroups() {
    this.environments.forEach((environment) => {
      if (!this.groupedEnvironments[environment['parent']]) {
        this.groupedEnvironments[environment['parent']] = [];
      }
      this.groupedEnvironments[environment['parent']].push(environment);
    });
  }

  private loadDeploymentParameters() {
    this.deploymentService.getAllDeploymentParameterKeys().subscribe({
      next: (r) =>
        (this.deploymentParameters = r.sort(function (a, b) {
          return a.key.localeCompare(b.key, undefined, { sensitivity: 'base' });
        })),
      error: (e) => (this.errorMessage = e),
    });
  }

  // for url params only
  private setPreselected() {
    if (this.appserverName) {
      this.selectedAppserver = _.find(this.appservers, {
        name: this.appserverName,
      });
      if (this.selectedAppserver) {
        this.resourceService.getDeployableReleases(this.selectedAppserver.id).subscribe({
          next: (r) => (this.releases = r),
          error: (e) => (this.errorMessage = e),
          complete: () => this.setRelease(),
        });
      }
    }
  }

  private prepareNewDeployment() {
    if (this.deploymentId) {
      this.appserverName = this.deploymentId.toString();
      delete this.deploymentId;
    }
    this.initAppservers();
  }

  private prepareRedeploy() {
    this.isRedeployment = true;
    this.getDeployment();
  }

  // for url params only
  private setRelease() {
    if (this.releaseName) {
      this.selectedRelease = this.releases.find((release) => release.release === this.releaseName);
      this.onChangeRelease();
    }
  }

  private goTo(destination: string) {
    this.location.go('/deployment/' + destination);
  }
}
