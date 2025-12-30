import { ChangeDetectionStrategy, Component, OnInit, AfterViewInit, OnDestroy, inject, signal } from '@angular/core';
import { Location } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import { ResourceService } from '../resources/services/resource.service';
import { ResourceTag } from '../resources/models/resource-tag';
import { Resource } from '../resources/models/resource';
import { Release } from '../resources/models/release';
import { Relation } from '../resources/models/relation';
import { Deployment } from './deployment';
import { DeploymentParameter } from './deployment-parameter';
import { DeploymentService } from './deployment.service';
import { EnvironmentService } from './environment.service';
import { Environment } from './environment';
import { DeploymentRequest } from './deployment-request';
import { AppWithVersion } from './app-with-version';
import { of, Subject, Subscription } from 'rxjs';
import * as _ from 'lodash-es';
import { DateTimeModel } from '../shared/date-time-picker/date-time.model';
import { IconComponent } from '../shared/icon/icon.component';
import { DateTimePickerComponent } from '../shared/date-time-picker/date-time-picker.component';
import { FormsModule } from '@angular/forms';
import { NgSelectModule } from '@ng-select/ng-select';
import { NotificationComponent } from '../shared/elements/notification/notification.component';
import { LoadingIndicatorComponent } from '../shared/elements/loading-indicator.component';
import { PageComponent } from '../layout/page/page.component';
import { ButtonComponent } from '../shared/button/button.component';
import { ResourceTypesService } from '../resources/services/resource-types.service';
import { toSignal } from '@angular/core/rxjs-interop';
import { switchMap, takeUntil } from 'rxjs/operators';

@Component({
  selector: 'app-deployment',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './deployment.component.html',
  imports: [
    LoadingIndicatorComponent,
    NotificationComponent,
    NgSelectModule,
    FormsModule,
    DateTimePickerComponent,
    IconComponent,
    PageComponent,
    ButtonComponent,
  ],
})
export class DeploymentComponent implements OnInit, AfterViewInit, OnDestroy {
  private resourceService = inject(ResourceService);
  private environmentService = inject(EnvironmentService);
  private deploymentService = inject(DeploymentService);
  private activatedRoute = inject(ActivatedRoute);
  private location = inject(Location);
  private resourceTypesService = inject(ResourceTypesService);
  private destroy$ = new Subject<void>();

  // from url
  appserverName: string = '';
  releaseName: string = '';
  // redeploy only
  deploymentId: number;

  // these are valid for all (loaded ony once)
  environments = signal<Environment[]>([]);
  groupedEnvironments = signal<{ [key: string]: Environment[] }>({});
  deploymentParameters = signal<DeploymentParameter[]>([]);
  defaultResourceTag: ResourceTag = { label: 'HEAD' } as ResourceTag;
  isRedeployment = signal(false);

  // per appserver/deployment request
  selectedAppserver = signal<Resource | null>(null);
  releases = signal<Release[]>([]);
  selectedRelease = signal<Release | null>(null);
  runtime = signal<Relation | null>(null);
  resourceTags = signal<ResourceTag[]>([this.defaultResourceTag]);
  selectedResourceTag = signal<ResourceTag>(this.defaultResourceTag);
  deploymentDate: DateTimeModel = null;
  appsWithVersion = signal<AppWithVersion[]>([]);
  transDeploymentParameter: DeploymentParameter = {} as DeploymentParameter;
  transDeploymentParameters: DeploymentParameter[] = [];
  deploymentResponse = signal<any>({});
  hasPermissionToDeploy = signal(false);
  hasPermissionToRequestDeployment = signal(false);

  // redeploy only
  selectedDeployment = signal<Deployment>({} as Deployment);
  redeploymentAppserverDisplayName = signal('');
  appsWithVersionForRedeployment = signal<AppWithVersion[]>([]);

  simulate: boolean = false;
  requestOnly: boolean = false;
  doSendEmail: boolean = false;

  bestForSelectedRelease = signal<Release | null>(null);

  errorMessage = signal('');
  successMessage = signal('');
  isLoading = signal(false);
  isDeploymentBlocked = signal(false);

  appServerResourceType$ = this.resourceTypesService.getResourceTypeByName('APPLICATIONSERVER');

  appservers = toSignal(
    this.appServerResourceType$.pipe(
      switchMap((resourceType) => (resourceType ? this.resourceService.getGroupsForType(resourceType) : of([]))),
    ),
    { initialValue: [] as Resource[] },
  );

  ngOnInit() {
    this.activatedRoute.params.pipe(takeUntil(this.destroy$)).subscribe((param: any) => {
      // Reset all component state when route params change
      this.resetComponentState();

      this.appserverName = param['appserverName'];
      this.releaseName = param['releaseName'];
      this.deploymentId = param['deploymentId'];

      // Initialize environments for each deployment
      this.initEnvironments().then(() => {
        if (this.deploymentId && Number(this.deploymentId)) {
          this.prepareRedeploy();
        } else {
          this.prepareNewDeployment();
        }
      });
    });
  }

  ngAfterViewInit() {
    // we don't need this right away
    this.loadDeploymentParameters();
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }

  initAppservers() {
    this.isLoading.set(true);
    this.setPreselected();
    this.isLoading.set(false);
  }

  onChangeAppserver() {
    this.resetVars();
    this.loadReleases();
    this.canDeploy();
  }

  onChangeRelease() {
    if (!this.selectedRelease()) {
      this.selectedRelease.set(this.releases()[0]);
    }
    this.getRelatedForRelease();
    this.goTo(this.selectedAppserver()?.name + '/' + this.selectedRelease().release);
  }

  onChangeEnvironment() {
    if (!this.isRedeployment()) {
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
      !this.isDeploymentBlocked() &&
      this.selectedRelease() !== null &&
      this.appsWithVersion().length > 0 &&
      _.filter(this.environments(), 'selected').length > 0
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
    return Object.keys(this.groupedEnvironments());
  }

  private getDeployment(): Subscription {
    return this.deploymentService.get(this.deploymentId).subscribe({
      next: (r) => this.selectedDeployment.set(r),
      error: (e) => this.errorMessage.set(e),
      complete: () => this.initRedeploymentValues(),
    });
  }

  private initRedeploymentValues() {
    this.isLoading.set(false);
    this.composeRedeploymentAppserverDisplayName();
    const deployment = this.selectedDeployment();
    this.transDeploymentParameters = deployment.deploymentParameters;
    this.appsWithVersion.set(deployment.appsWithVersion);
    this.selectedAppserver.set({
      id: deployment.appServerId,
      name: deployment.appServerName,
    } as Resource);
    this.loadReleases();
    this.setPreSelectedEnvironment();
    this.canDeploy();
  }

  private composeRedeploymentAppserverDisplayName() {
    const deployment = this.selectedDeployment();
    let displayName = '<h5>' + deployment.appServerName + ' (' + deployment.releaseName + ')' + '</h5>';
    deployment.appsWithVersion.forEach((appWithVersion) => {
      displayName += '<h6>' + appWithVersion.applicationName + ' (' + appWithVersion.version + ')' + '</h6>';
    });
    this.redeploymentAppserverDisplayName.set(displayName);
  }

  private setPreSelectedEnvironment() {
    const env = _.find(this.environments(), {
      name: this.selectedDeployment().environmentName,
    });
    if (env) {
      env.selected = true;
      this.environments.set([...this.environments()]);
    }
  }

  private setSelectedRelease(): Subscription {
    return this.resourceService.getMostRelevantRelease(this.selectedAppserver().id).subscribe({
      next: (r) => {
        this.selectedRelease.set(this.releases().find((release) => release.release === r.release));
      },
      error: (e) => this.errorMessage.set(e),
      complete: () => this.onChangeRelease(),
    });
  }

  private setSelectedReleaseForRedeployment() {
    this.selectedRelease.set(
      this.releases().find((release) => release.release === this.selectedDeployment().releaseName),
    );
    // will perform verifyRedeployment()
    this.getAppVersions();
  }

  private loadReleases(): Subscription {
    return this.resourceService.getDeployableReleases(this.selectedAppserver().id).subscribe({
      next: (r) => {
        this.releases.set(r);
      },
      error: (e) => this.errorMessage.set(e),
      complete: () => (this.isRedeployment() ? this.setSelectedReleaseForRedeployment() : this.setSelectedRelease()),
    });
  }

  private getRelatedForRelease() {
    this.resourceService.getLatestForRelease(this.selectedAppserver().id, this.selectedRelease().id).subscribe({
      next: (r) => {
        this.bestForSelectedRelease.set(r);
      },
      error: (e) => this.errorMessage.set(e),
      complete: () => this.extractFromRelations(),
    });
  }

  private extractFromRelations() {
    const best = this.bestForSelectedRelease();
    if (!best) {
      return;
    }
    this.runtime.set(_.filter(best.relations, { type: 'RUNTIME' }).pop());
    this.resourceTags.set([this.defaultResourceTag, ...(best.resourceTags || [])]);
    this.appsWithVersion.set([]);
    this.getAppVersions();
  }

  private getAppVersions() {
    this.resourceService
      .getAppsWithVersions(
        this.selectedAppserver().id,
        this.selectedRelease().id,
        _.filter(this.environments(), 'selected').map((val: Environment) => val.id),
      )
      .subscribe({
        next: (r) => {
          if (this.isRedeployment()) {
            this.appsWithVersionForRedeployment.set(r);
          } else {
            this.appsWithVersion.set(r);
          }
        },
        error: (e) => this.errorMessage.set(e),
        complete: () => (this.isRedeployment() ? this.verifyRedeployment() : ''),
      });
  }

  private resetVars() {
    this.errorMessage.set('');
    this.successMessage.set('');
    this.isDeploymentBlocked.set(false);
    this.selectedRelease.set(null);
    this.bestForSelectedRelease.set(null);
    this.resourceTags.set([this.defaultResourceTag]);
    this.selectedResourceTag.set(this.defaultResourceTag);
    this.deploymentDate = null;
    this.simulate = false;
    this.doSendEmail = false;
    this.appsWithVersion.set([]);
    this.transDeploymentParameter = {} as DeploymentParameter;
    this.transDeploymentParameters = [];
  }

  private resetComponentState() {
    // Reset all state when switching between deployments
    this.isRedeployment.set(false);
    this.selectedAppserver.set(null);
    this.releases.set([]);
    this.selectedRelease.set(null);
    this.runtime.set(null);
    this.selectedDeployment.set({} as Deployment);
    this.redeploymentAppserverDisplayName.set('');
    this.appsWithVersionForRedeployment.set([]);
    this.resetVars();
    // Environments will be reloaded by initEnvironments()
  }

  private canDeploy() {
    if (this.selectedAppserver() != null) {
      this.hasPermissionToDeploy.set(false);
      const contextIds: number[] = _.filter(this.environments(), 'selected').map((val: Environment) => val.id);
      if (contextIds.length > 0) {
        this.deploymentService.canDeploy(this.selectedAppserver().id, contextIds).subscribe({
          next: (r) => {
            this.hasPermissionToDeploy.set(r);
          },
          error: (e) => this.errorMessage.set(e),
          complete: () => this.canRequestDeployment(contextIds),
        });
      }
    }
  }

  private canRequestDeployment(contextIds: number[]) {
    if (this.selectedAppserver() != null) {
      this.hasPermissionToRequestDeployment.set(false);
      if (contextIds.length > 0) {
        this.deploymentService.canRequestDeployment(this.selectedAppserver().id, contextIds).subscribe({
          next: (r) => {
            this.hasPermissionToRequestDeployment.set(r);
          },
          error: (e) => this.errorMessage.set(e),
        });
      }
    }
  }

  private verifyRedeployment() {
    this.errorMessage.set('');
    this.isDeploymentBlocked.set(false);
    this.appsWithVersion().forEach((originApp: AppWithVersion) => {
      const actualApp: AppWithVersion = _.find(this.appsWithVersionForRedeployment(), [
        'applicationName',
        originApp.applicationName,
      ]);
      if (!this.isDeploymentBlocked() && !actualApp) {
        this.errorMessage.set('Application <strong>' + originApp.applicationName + '</strong> does not exist anymore');
        this.isDeploymentBlocked.set(true);
      }
    });
  }

  private prepareDeployment() {
    if (this.isReadyForDeployment()) {
      const contextIds: number[] = _.filter(this.environments(), 'selected').map((val: Environment) => val.id);
      this.createDeploymentRequest(contextIds);
    }
  }

  private createDeploymentRequest(contextIds: number[]) {
    this.isLoading.set(true);
    this.errorMessage.set('');
    this.successMessage.set('');
    const deploymentRequest: DeploymentRequest = {} as DeploymentRequest;
    deploymentRequest.appServerName = this.selectedAppserver()?.name;
    deploymentRequest.releaseName = this.selectedRelease().release;
    deploymentRequest.contextIds = contextIds;
    deploymentRequest.simulate = this.simulate;
    deploymentRequest.sendEmail = this.doSendEmail;
    deploymentRequest.requestOnly = this.requestOnly;
    deploymentRequest.appsWithVersion = this.appsWithVersion();
    if (!this.isRedeployment()) {
      deploymentRequest.stateToDeploy =
        this.selectedResourceTag() && this.selectedResourceTag().tagDate
          ? this.selectedResourceTag().tagDate
          : new Date().getTime();
    }
    if (this.deploymentDate) {
      deploymentRequest.deploymentDate = this.deploymentDate.toEpoch();
    }
    if (this.transDeploymentParameters.length > 0) {
      deploymentRequest.deploymentParameters = this.transDeploymentParameters;
    }
    this.deploymentService.createDeployment(deploymentRequest).subscribe({
      next: (r) => this.deploymentResponse.set(r),
      error: (e) => this.errorMessage.set(e),
      complete: () => {
        this.isLoading.set(false);
        this.composeSuccessMessage();
      },
    });
  }

  private composeSuccessMessage() {
    const link = `<a href="#/deployments?filters=[{%22name%22:%22Tracking%20Id%22,%22val%22:%22${this.deploymentResponse().trackingId}%22}]">Tracking Id ${this.deploymentResponse().trackingId}</a>`;
    this.successMessage.set('Deployment created: <strong>' + link + '</strong>');
  }

  private initEnvironments(): Promise<void> {
    return new Promise((resolve) => {
      this.isLoading.set(true);
      this.environmentService.getAll().subscribe({
        next: (r) => {
          this.environments.set(r);
          this.groupedEnvironments.set({}); // Clear previous grouping
          this.extractEnvironmentGroups();
        },
        error: (e) => {
          this.errorMessage.set(e);
          this.isLoading.set(false);
          resolve();
        },
        complete: () => {
          this.isLoading.set(false);
          resolve();
        },
      });
    });
  }

  private extractEnvironmentGroups() {
    const grouped: { [key: string]: Environment[] } = {};
    this.environments().forEach((environment) => {
      if (!grouped[environment['parentName']]) {
        grouped[environment['parentName']] = [];
      }
      grouped[environment['parentName']].push(environment);
    });
    this.groupedEnvironments.set(grouped);
  }

  private loadDeploymentParameters() {
    this.deploymentService.getAllDeploymentParameterKeys().subscribe({
      next: (r) =>
        this.deploymentParameters.set(
          r.sort(function (a, b) {
            return a.key.localeCompare(b.key, undefined, { sensitivity: 'base' });
          }),
        ),
      error: (e) => this.errorMessage.set(e),
    });
  }

  // for url params only
  private setPreselected() {
    if (this.appserverName) {
      this.selectedAppserver.set(
        _.find(this.appservers(), {
          name: this.appserverName,
        }),
      );
      if (this.selectedAppserver()) {
        this.resourceService.getDeployableReleases(this.selectedAppserver().id).subscribe({
          next: (r) => {
            this.releases.set(r);
          },
          error: (e) => this.errorMessage.set(e),
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
    this.isRedeployment.set(true);
    this.getDeployment();
  }

  // for url params only
  private setRelease() {
    if (this.releaseName) {
      this.selectedRelease.set(this.releases().find((release) => release.release === this.releaseName));
      this.onChangeRelease();
    }
  }

  private goTo(destination: string) {
    this.location.go('/deployment/' + destination);
  }
}
