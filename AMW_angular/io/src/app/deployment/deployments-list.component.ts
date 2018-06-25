import { Component, Input, Output, EventEmitter, NgZone } from '@angular/core';
import { ResourceService } from '../resource/resource.service';
import { Deployment } from './deployment';
import { DeploymentFilter } from './deployment-filter';
import { DeploymentService } from './deployment.service';
import { Datetimepicker } from 'eonasdan-bootstrap-datetimepicker';
import * as _ from 'lodash';
import * as moment from 'moment';

declare var $: any;

@Component({
  selector: 'amw-deployments-list',
  templateUrl: './deployments-list.component.html'
})

export class DeploymentsListComponent {

  @Input() deployments: Deployment[] = [];
  @Input() sortCol: string;
  @Input() sortDirection: string;
  @Input() filtersForParam: DeploymentFilter[];
  @Output() editDeploymentDate: EventEmitter<Deployment> = new EventEmitter<Deployment>();
  @Output() selectAllDeployments: EventEmitter<boolean> = new EventEmitter<boolean>();
  @Output() doCancelDeployment: EventEmitter<Deployment> = new EventEmitter<Deployment>();
  @Output() doRejectDeployment: EventEmitter<Deployment> = new EventEmitter<Deployment>();
  @Output() doConfirmDeployment: EventEmitter<Deployment> = new EventEmitter<Deployment>();
  @Output() doSort: EventEmitter<string> = new EventEmitter<string>();

  deployment: Deployment;

  deploymentDate: number;

  hasPermissionShakedownTest: boolean = null;

  errorMessage: string = '';

  allSelected: boolean = false;

  failureReason: { [key: string]: string } = { PRE_DEPLOYMENT_GENERATION: 'pre deployment generation failed',
    DEPLOYMENT_GENERATION: 'deployment generation failed', PRE_DEPLOYMENT_SCRIPT: 'pre deployment script failed',
    DEPLOYMENT_SCRIPT: 'deployment script failed', NODE_MISSING: 'no nodes enabled', TIMEOUT: 'timeout',
    UNEXPECTED_ERROR: 'unexpected error', RUNTIME_ERROR: 'runtime error' };

  constructor(private ngZone: NgZone,
              private deploymentService: DeploymentService,
              private resourceService: ResourceService) {
  }

  showDetails(deploymentId: number) {
    this.deployment = _.find(this.deployments, ['id', deploymentId]);
    $('#deploymentDetails').modal('show');
  }

  showDateChange(deploymentId: number) {
    this.deployment = _.find(this.deployments, ['id', deploymentId]);
    $('#deploymentDateChange').modal('show');
    this.ngZone.onMicrotaskEmpty.first().subscribe(() => {
      $('.datepicker').datetimepicker({format: 'DD.MM.YYYY HH:mm'});
    });
  }

  showConfirm(deploymentId: number) {
    this.deployment = _.find(this.deployments, ['id', deploymentId]);
    this.resourceService.canCreateShakedownTest(this.deployment.appServerId).subscribe(
      /* happy path */ (r) => this.hasPermissionShakedownTest = r,
      /* error path */ (e) => this.errorMessage = e,
      /* onComplete */  () => $('#deploymentConfirmation').modal('show'));
  }

  showReject(deploymentId: number) {
    this.deployment = _.find(this.deployments, ['id', deploymentId]);
    $('#deploymentRejection').modal('show');
  }

  showCancel(deploymentId: number) {
    this.deployment = _.find(this.deployments, ['id', deploymentId]);
    $('#deploymentCancelation').modal('show');
  }

  doDateChange() {
    if (this.deployment) {
      this.errorMessage = '';
      const dateTime = moment(this.deploymentDate, 'DD.MM.YYYY hh:mm');
      if (!dateTime || !dateTime.isValid()) {
        this.errorMessage = 'Invalid date';
      } else {
        this.deployment.deploymentDate = dateTime.valueOf();
        this.editDeploymentDate.emit(this.deployment);
        $('#deploymentDateChange').modal('hide');
        delete this.deployment;
        delete this.deploymentDate;
      }
    }
  }

  doReject() {
    if (this.deployment) {
      this.doRejectDeployment.emit(this.deployment);
      $('#deploymentRejection').modal('hide');
      delete this.deployment;
    }
  }

  doCancel() {
    if (this.deployment) {
      this.doCancelDeployment.emit(this.deployment);
      $('#deploymentCancelation').modal('hide');
      delete this.deployment;
    }
  }

  doConfirm() {
    if (this.deployment) {
      this.doConfirmDeployment.emit(this.deployment);
      $('#deploymentConfirmation').modal('hide');
      delete this.deployment;
    }
  }

  reSort(col: string) {
    this.doSort.emit(col);
  }

  switchAllDeployments() {
    this.allSelected = !this.allSelected;
    this.selectAllDeployments.emit(this.allSelected);
  }

  appServerLink(appServerId: number) {
    if (appServerId) {
      window.location.href = '/AMW_web/pages/editResourceView.xhtml?id=' + appServerId + '&ctx=1';
    }
  }

  appLink(appId: number) {
    this.resourceService.resourceExists(appId).subscribe(
      /* happy path */ (r) =>  { if (r) { window.location.href = '/AMW_web/pages/editResourceView.xhtml?id=' + appId + '&ctx=1'; } }
    );
  }

  logViewerLink(deploymentId: number) {
    window.location.href = '/AMW_web/pages/logView.xhtml?deploymentId=' + deploymentId + '&filters=' + encodeURI(JSON.stringify(this.filtersForParam));
  }

}
