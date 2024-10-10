import { Component, Input, Output, EventEmitter } from '@angular/core';
import { Deployment } from '../deployment/deployment';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { DateTimeModel } from '../shared/date-time-picker/date-time.model';
import { DateTimePickerComponent } from '../shared/date-time-picker/date-time-picker.component';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-deployments-edit-modal',
  templateUrl: './deployments-edit-modal.component.html',
  standalone: true,
  imports: [FormsModule, DateTimePickerComponent],
})
export class DeploymentsEditModalComponent {
  @Input() deployments: Deployment[] = [];

  @Output() errorMessage: EventEmitter<string> = new EventEmitter<string>();
  @Output() doConfirmDeployment: EventEmitter<Deployment> = new EventEmitter<Deployment>();
  @Output() doRejectDeployment: EventEmitter<Deployment> = new EventEmitter<Deployment>();
  @Output() doCancelDeployment: EventEmitter<Deployment> = new EventEmitter<Deployment>();
  @Output() doEditDeploymentDate: EventEmitter<Deployment> = new EventEmitter<Deployment>();

  confirmationAttributes: Deployment;
  deploymentDate: DateTimeModel;
  selectedEditAction: string;
  editActions: string[] = ['Change date', 'Confirm', 'Reject', 'Cancel'];

  constructor(public activeModal: NgbActiveModal) {
    this.confirmationAttributes = {} as Deployment;
    this.activeModal = activeModal;
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
    this.activeModal.close('Close click');
  }

  private clear() {
    this.confirmationAttributes = {} as Deployment;
    this.selectedEditAction = '';
    delete this.deploymentDate;
  }

  private confirmSelectedDeployments() {
    this.editDeploymentDate(false);
    for (const deployment of this.deployments) {
      deployment.sendEmailWhenDeployed = this.confirmationAttributes.sendEmailWhenDeployed;
      deployment.simulateBeforeDeployment = this.confirmationAttributes.simulateBeforeDeployment;
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
    if (!this.deploymentDate) {
      this.errorMessage.emit('Invalid date');
    } else {
      for (const deployment of this.deployments) {
        deployment.deploymentDate = this.deploymentDate.toEpoch();
        if (emit) {
          this.doEditDeploymentDate.emit(deployment);
        }
      }
    }
  }
}
