import { Component, Input, Output, EventEmitter, NgZone } from '@angular/core';
import { Deployment } from './deployment';
import { DeploymentStateMessage } from './deployment-state-message';
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

  deployment: Deployment;

  deploymentDate: number;

  deploymentStateMessage: DeploymentStateMessage;

  errorMessage: string = '';

  allSelected: boolean = false;

  constructor(private ngZone: NgZone,
              private deploymentService: DeploymentService) {
  }

  showDetails(deploymentId: number) {
    delete this.deploymentStateMessage;
    this.deployment = _.find(this.deployments, ['id', deploymentId]);
    this.getDeploymentStateMessage(deploymentId);
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
    $('#deploymentConfirmation').modal('show');
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
      console.log('TODO: reject deployment');
      $('#deploymentRejection').modal('hide');
      delete this.deployment;
    }
  }

  doCancel() {
    if (this.deployment) {
      console.log('TODO: cancel deployment??');
      this.deploymentService.cancelDeployment(this.deployment.id).subscribe(
        /* happy path */ (r) => r,
        /* error path */ (e) => this.errorMessage = e
      );
      $('#deploymentCancelation').modal('hide');
      delete this.deployment;
    }
  }

  switchAllDeployments() {
    this.allSelected = !this.allSelected;
    this.selectAllDeployments.emit(this.allSelected);
  }

  private getDeploymentStateMessage(deploymentId: number) {
    this.deploymentService.getDeploymentStateMessage(deploymentId).subscribe(
      /* happy path */ (r) => this.deploymentStateMessage = r,
      /* error path */ (e) => this.errorMessage = e);
  }

}
