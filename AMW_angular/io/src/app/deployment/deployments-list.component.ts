import { Component, Input, Output, EventEmitter, NgZone } from '@angular/core';
import { Deployment } from './deployment';
import { DeploymentDetail } from './deployment-detail';
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
  @Output() editDeploymentDate: EventEmitter<Deployment> = new EventEmitter<Deployment>();
  @Output() selectAllDeployments: EventEmitter<boolean> = new EventEmitter<boolean>();
  @Output() doCancelDeployment: EventEmitter<Deployment> = new EventEmitter<Deployment>();
  @Output() doRejectDeployment: EventEmitter<Deployment> = new EventEmitter<Deployment>();

  deployment: Deployment;

  deploymentDate: number;

  deploymentDetail: DeploymentDetail;

  errorMessage: string = '';

  allSelected: boolean = false;

  constructor(private ngZone: NgZone,
              private deploymentService: DeploymentService) {
  }

  showDetails(deploymentId: number) {
    delete this.deploymentDetail;
    this.deployment = _.find(this.deployments, ['id', deploymentId]);
    this.getDeploymentDetail(deploymentId);
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
    // this.getDeploymentDetail(deploymentId);
    this.deploymentService.getDeploymentDetail(deploymentId).subscribe(
      /* happy path */ (r) => this.setConfirmValuesOnDeployment(r),
      /* error path */ (e) => this.errorMessage = e,
                        () => $('#deploymentConfirmation').modal('show'));

  }

  setConfirmValuesOnDeployment(deploymentDetail: DeploymentDetail) {
    this.deployment.sendEmailWhenDeployed = deploymentDetail.sendEmailWhenDeployed;
    this.deployment.simulateBeforeDeployment = deploymentDetail.simulateBeforeDeployment;
    this.deployment.shakedownTestsWhenDeployed = deploymentDetail.shakedownTestsWhenDeployed;
    this.deployment.neighbourhoodTest = deploymentDetail.neighbourhoodTest;
  }

  showReject(deploymentId: number) {
    this.deployment = _.find(this.deployments, ['id', deploymentId]);
    $('#deploymentRejection').modal('show');
  }

  showCancel(deploymentId: number) {
    this.deployment = _.find(this.deployments, ['id', deploymentId]);
    $('#deploymentCancelation').modal('show');
  }

  doConfirm() {
    if (this.deployment) {
      console.log('TODO: confirm deployment');
      $('#deploymentConfirmation').modal('hide');
      delete this.deployment;
    }
  }

  doDateChange() {
    if (this.deployment) {
      this.errorMessage = '';
      let dateTime = moment(this.deploymentDate, 'DD.MM.YYYY hh:mm');
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

  switchAllDeployments() {
    this.allSelected = !this.allSelected;
    this.selectAllDeployments.emit(this.allSelected);
  }

  private getDeploymentDetail(deploymentId: number) {
    this.deploymentService.getDeploymentDetail(deploymentId).subscribe(
      /* happy path */ (r) => this.deploymentDetail = r,
      /* error path */ (e) => this.errorMessage = e);
  }

}
