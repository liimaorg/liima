import { Component, Input, Output, EventEmitter, NgZone } from '@angular/core';
import { Deployment } from './deployment';
import { Datetimepicker } from 'eonasdan-bootstrap-datetimepicker';
import * as moment from 'moment';

declare var $: any;

@Component({
  selector: 'amw-deployments-edit-modal',
  templateUrl: './deployments-edit-modal.component.html'
})

export class DeploymentsEditModalComponent {

  @Input() deployments: Deployment[] = [];
  @Input() editActions: string[];
  @Input() hasPermissionShakedownTest: boolean;

  @Output() errorMessage: EventEmitter<string> = new EventEmitter<string>();
  @Output() doConfirmDeployment: EventEmitter<Deployment> = new EventEmitter<Deployment>();
  @Output() doRejectDeployment: EventEmitter<Deployment> = new EventEmitter<Deployment>();
  @Output() doCancelDeployment: EventEmitter<Deployment> = new EventEmitter<Deployment>();
  @Output() doEditDeploymentDate: EventEmitter<Deployment> = new EventEmitter<Deployment>();

  confirmationAttributes: Deployment;
  deploymentDate: string; // for deployment date change in during confirmation (format 'DD.MM.YYYY HH:mm')
  selectedEditAction: string;

  constructor(private ngZone: NgZone) {
    this.confirmationAttributes = {} as Deployment;
  }

  changeEditAction() {
    const isConfirm = this.selectedEditAction === this.editActions[1];
    const isEditDeploymentDate = this.selectedEditAction === this.editActions[0];
    if (isConfirm || isEditDeploymentDate) {
      this.addDatePicker();
    }
  }

  doEdit() {
    switch (this.selectedEditAction) {
      // date
      case this.editActions[0]:
        this.editDeploymentDate(true);
        break;
      // confirm
      case this.editActions[1]:
        this.confirmSelectedDeployments();
        break;
      // reject
      case this.editActions[2]:
        this.rejectSelectedDeployments();
        break;
      // cancel
      case this.editActions[3]:
        this.cancelSelectedDeployments();
        break;
      default:
        console.error('Unknown EditAction' + this.selectedEditAction);
        break;
    }
    this.clear();
    $('#deploymentsEdit').modal('hide');
  }

  private clear() {
    this.confirmationAttributes = {} as Deployment;
    this.selectedEditAction = '';
    this.deploymentDate = '';
  }

  private confirmSelectedDeployments() {
    this.editDeploymentDate(false);
    for (const deployment of this.deployments) {
      deployment.sendEmailWhenDeployed = this.confirmationAttributes.sendEmailWhenDeployed;
      deployment.simulateBeforeDeployment = this.confirmationAttributes.simulateBeforeDeployment;
      deployment.shakedownTestsWhenDeployed = this.confirmationAttributes.shakedownTestsWhenDeployed;
      deployment.neighbourhoodTest = this.confirmationAttributes.neighbourhoodTest;
      this.doConfirmDeployment.emit(deployment);
    }
  }

  private rejectSelectedDeployments() {
    for (const deployment of this.deployments) {
      this.doRejectDeployment.emit(deployment);
    }
  }

  private cancelSelectedDeployments() {
    for (const deployment of this.deployments) {
      this.doCancelDeployment.emit(deployment);
    }
  }

  private editDeploymentDate(emit: boolean) {
    const dateTime = moment(this.deploymentDate, 'DD.MM.YYYY HH:mm');
    if (!dateTime || !dateTime.isValid()) {
      this.errorMessage.emit('Invalid date');
    } else {
      for (const deployment of this.deployments) {
        deployment.deploymentDate = dateTime.valueOf();
        if (emit) {
          this.doEditDeploymentDate.emit(deployment);
        }
      }
    }
  }

  private addDatePicker() {
    this.ngZone.onMicrotaskEmpty.first().subscribe(() => {
      $('.datepicker').datetimepicker({format: 'DD.MM.YYYY HH:mm'});
    });
  }
}
