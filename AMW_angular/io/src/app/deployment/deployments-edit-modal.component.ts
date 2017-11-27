import { Component, Input, Output, EventEmitter, NgZone } from '@angular/core';
import { ResourceService } from '../resource/resource.service';
import { Deployment } from './deployment';
import { DeploymentDetail } from './deployment-detail';
import { DeploymentService } from './deployment.service';
import { Datetimepicker } from 'eonasdan-bootstrap-datetimepicker';
import * as _ from 'lodash';
import * as moment from 'moment';

declare var $: any;

@Component({
  selector: 'amw-deployments-edit-modal',
  templateUrl: './deployments-edit-modal.component.html'
})

export class DeploymentsEditModalComponent {

  @Input() deployments: Deployment[] = [];
  @Input() selectedEditAction: string;
  @Input() editActions: string[];
  @Input() deploymentDetailMap: { [key: number]: DeploymentDetail };
  // @Input() deploymentDate:


  @Output() editDeploymentDate: EventEmitter<Deployment> = new EventEmitter<Deployment>();
  @Output() errorMessage: EventEmitter<string> = new EventEmitter<string>();
  @Output() doReloadDeployment: EventEmitter<number> = new EventEmitter<number>();

  confirmationAttributes: DeploymentDetail;
  deploymentDate: number; // for deployment date change



  constructor(private ngZone: NgZone,
              private deploymentService: DeploymentService,
              private resourceService: ResourceService) {
    this.confirmationAttributes = <DeploymentDetail> {};
  }

  changeEditAction() {
    if (this.selectedEditAction === 'Change date') {
      this.addDatePicker();
    }
  }

  doEdit() {
    if (this.editableDeployments()) {
      // this.errorMessage = '';
      switch (this.selectedEditAction) {
        // date
        case this.editActions[0]:
          this.setSelectedDeploymentDates();
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
      $('#deploymentsEdit').modal('hide');
    }
  }


  confirmDeployment(deploymentDetail: DeploymentDetail) {
    if (deploymentDetail) {
      this.deploymentService.confirmDeployment(deploymentDetail).subscribe(
        /* happy path */ (r) => r,
        /* error path */ (e) => this.errorMessage = this.errorMessage ? this.errorMessage + '<br>' + e : e,
        /* onComplete */ () => this.doReloadDeployment.emit(deploymentDetail.deploymentId)
      );
    }
  }

  rejectDeployment(deployment: Deployment) {
    if (deployment) {
      this.deploymentService.rejectDeployment(deployment.id).subscribe(
        /* happy path */ (r) => r,
        /* error path */ (e) => this.errorMessage = this.errorMessage ? this.errorMessage + '<br>' + e : e,
        /* onComplete */ () => this.doReloadDeployment.emit(deployment.id)
      );
    }
  }

  cancelDeployment(deployment: Deployment) {
    if (deployment) {
      this.deploymentService.cancelDeployment(deployment.id).subscribe(
        /* happy path */ (r) => r,
        /* error path */ (e) => this.errorMessage = this.errorMessage ? this.errorMessage + '<br>' + e : e,
        /* onComplete */ () => this.doReloadDeployment.emit(deployment.id)
      );
    }
  }

  editableDeployments(): boolean {
    return (_.findIndex(this.deployments, {selected: true}) !== -1);
  }


  changeDeploymentDate(deployment: Deployment) {
    if (deployment) {
      this.setDeploymentDate(deployment, deployment.deploymentDate);
    }
  }

  private applyConfirmationAttributesIntoDeploymentDetailAndDoConfirm(deploymentId: number) {
    let deploymentDetail = this.deploymentDetailMap[deploymentId];
    deploymentDetail.sendEmailWhenDeployed = this.confirmationAttributes.sendEmailWhenDeployed;
    deploymentDetail.simulateBeforeDeployment = this.confirmationAttributes.simulateBeforeDeployment;
    deploymentDetail.shakedownTestsWhenDeployed = this.confirmationAttributes.shakedownTestsWhenDeployed;
    deploymentDetail.neighbourhoodTest = this.confirmationAttributes.neighbourhoodTest;
    this.confirmDeployment(deploymentDetail);
  }

  private confirmSelectedDeployments() {
    this.setSelectedDeploymentDates();
    this.deployments.filter((deployment) => deployment.selected === true).forEach((deployment) => {
      this.deploymentService.getDeploymentDetail(deployment.id).subscribe(
        /* happy path */ (r) => this.deploymentDetailMap[deployment.id] = r,
        // /* happy path */ (r) => this.deploymentDetailMap[deployment.id] = r,
        /* error path */ (e) => e,
        /* on complete */ () => this.applyConfirmationAttributesIntoDeploymentDetailAndDoConfirm(deployment.id)
      );
    });
  }

  private rejectSelectedDeployments() {
    this.deployments.filter((deployment) => deployment.selected === true).forEach((deployment) => {
      this.rejectDeployment(deployment);
    });
  }

  private cancelSelectedDeployments() {
    this.deployments.filter((deployment) => deployment.selected === true).forEach((deployment) => {
      this.cancelDeployment(deployment);
    });
  }


  private setSelectedDeploymentDates() {
    let dateTime = moment(this.deploymentDate, 'DD.MM.YYYY HH:mm');
    if (!dateTime || !dateTime.isValid()) {
      this.errorMessage.emit('Invalid date');
    } else {
      _.filter(this.deployments, {selected: true}).forEach((deployment) => this.setDeploymentDate(deployment, dateTime.valueOf()));
    }
  }


  private setDeploymentDate(deployment: Deployment, deploymentDate: number) {
    this.deploymentService.setDeploymentDate(deployment.id, deploymentDate).subscribe(
      /* happy path */ (r) => r,
      /* error path */ (e) => this.errorMessage = this.errorMessage ? this.errorMessage + '<br>' + e : e,
      /* on complete */ () => this.doReloadDeployment.emit(deployment.id)
    );
  }

  private addDatePicker() {
    this.ngZone.onMicrotaskEmpty.first().subscribe(() => {
      $('.datepicker').datetimepicker({format: 'DD.MM.YYYY HH:mm'});
    });
  }
}
