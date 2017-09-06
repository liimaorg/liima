import { Component, Input } from '@angular/core';
import { Deployment } from './deployment';
import * as _ from 'lodash';

declare var $: any;

@Component({
  selector: 'amw-deployments-list',
  templateUrl: './deployments-list.component.html'
})

export class DeploymentsListComponent {

  @Input() deployments: Deployment[] = [];

  deployment: Deployment;

  showDetails(deploymentId: number) {
    this.deployment = _.find(this.deployments, ['id', deploymentId]);
    console.log('TODO: fetch some additional information for deployment ' +deploymentId);
    $("#deploymentDetails").modal('show');
  }

  showEdit(deploymentId: number) {
    this.deployment = _.find(this.deployments, ['id', deploymentId]);
    $("#deploymentModification").modal('show');
  }

  showConfirm(deploymentId: number) {
    this.deployment = _.find(this.deployments, ['id', deploymentId]);
    $("#deploymentConfirmation").modal('show');
  }

  showReject(deploymentId: number) {
    this.deployment = _.find(this.deployments, ['id', deploymentId]);
    $("#deploymentRejection").modal('show');
  }

  showCancel(deploymentId: number) {
    this.deployment = _.find(this.deployments, ['id', deploymentId]);
    $("#deploymentCancelation").modal('show');
  }

  doConfirm() {
    if (this.deployment) {
      console.log('TODO: confirm deployment');
      $("#deploymentConfirmation").modal('hide');
      delete this.deployment;
    }
  }

  doEdit() {
    if (this.deployment) {
      console.log('TODO: edit deployment date');
      $("#deploymentModification").modal('hide');
      delete this.deployment;
    }
  }

  doReject() {
    if (this.deployment) {
      console.log('TODO: reject deployment');
      $("#deploymentRejection").modal('hide');
      delete this.deployment;
    }
  }

  doCancel() {
    if (this.deployment) {
      console.log('TODO: cancel deployment');
      $("#deploymentCancelation").modal('hide');
      delete this.deployment;
    }
  }

}
