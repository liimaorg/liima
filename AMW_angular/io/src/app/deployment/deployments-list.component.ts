import { Component, Input, Output, EventEmitter, NgZone } from '@angular/core';
import { Deployment } from './deployment';
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

  deployment: Deployment;

  deploymentDate: number;

  errorMessage: string = '';

  constructor(private ngZone: NgZone) {
  }

  showDetails(deploymentId: number) {
    this.deployment = _.find(this.deployments, ['id', deploymentId]);
    console.log('TODO: fetch some additional information for deployment ' + deploymentId);
    $('#deploymentDetails').modal('show');
  }

  showEdit(deploymentId: number) {
    this.deployment = _.find(this.deployments, ['id', deploymentId]);
    $('#deploymentModification').modal('show');
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

  doEdit() {
    if (this.deployment) {
      this.errorMessage = '';
      let dateTime = moment(this.deploymentDate, 'DD.MM.YYYY hh:mm');
      if (!dateTime || !dateTime.isValid()) {
        this.errorMessage = 'Invalid date';
      } else {
        this.deployment.deploymentDate = dateTime.valueOf();
        this.editDeploymentDate.emit(this.deployment);
        $('#deploymentModification').modal('hide');
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
      console.log('TODO: cancel deployment');
      $('#deploymentCancelation').modal('hide');
      delete this.deployment;
    }
  }

}
