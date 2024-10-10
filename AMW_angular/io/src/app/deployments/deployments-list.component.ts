import { Component, Input, Output, EventEmitter } from '@angular/core';
import { Deployment } from '../deployment/deployment';
import { DeploymentFilter } from '../deployment/deployment-filter';
import { ResourceService } from '../resource/resource.service';
import * as _ from 'lodash';
import { DateTimeModel } from '../shared/date-time-picker/date-time.model';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { DATE_TIME_FORMAT } from '../core/amw-constants';
import { DateTimePickerComponent } from '../shared/date-time-picker/date-time-picker.component';
import { RouterLink } from '@angular/router';
import { IconComponent } from '../shared/icon/icon.component';
import { FormsModule } from '@angular/forms';
import { SortableIconComponent } from '../shared/sortable-icon/sortable-icon.component';
import { DatePipe } from '@angular/common';

@Component({
  selector: 'app-deployments-list',
  templateUrl: './deployments-list.component.html',
  standalone: true,
  imports: [SortableIconComponent, FormsModule, IconComponent, RouterLink, DateTimePickerComponent, DatePipe],
})
export class DeploymentsListComponent {
  @Input() deployments: Deployment[] = [];
  @Input() sortCol: string;
  @Input() sortDirection: 'ASC';
  @Input() filtersForParam: DeploymentFilter[];
  @Output() editDeploymentDate: EventEmitter<Deployment> = new EventEmitter<Deployment>();
  @Output() selectAllDeployments: EventEmitter<boolean> = new EventEmitter<boolean>();
  @Output() doCancelDeployment: EventEmitter<Deployment> = new EventEmitter<Deployment>();
  @Output() doRejectDeployment: EventEmitter<Deployment> = new EventEmitter<Deployment>();
  @Output() doConfirmDeployment: EventEmitter<Deployment> = new EventEmitter<Deployment>();
  @Output() doSort: EventEmitter<string> = new EventEmitter<string>();

  deployment: Deployment;
  deploymentDate: DateTimeModel = new DateTimeModel();
  allSelected: boolean = false;
  // TODO: show this error somewhere?
  errorMessage = '';
  dateFormat = DATE_TIME_FORMAT;

  failureReason: { [key: string]: string } = {
    PRE_DEPLOYMENT_GENERATION: 'pre deployment generation failed',
    DEPLOYMENT_GENERATION: 'deployment generation failed',
    PRE_DEPLOYMENT_SCRIPT: 'pre deployment script failed',
    DEPLOYMENT_SCRIPT: 'deployment script failed',
    NODE_MISSING: 'no nodes enabled',
    TIMEOUT: 'timeout',
    UNEXPECTED_ERROR: 'unexpected error',
    RUNTIME_ERROR: 'runtime error',
  };

  constructor(
    private resourceService: ResourceService,
    private modalService: NgbModal,
  ) {}

  showDetails(content, deploymentId: number) {
    this.deployment = _.find(this.deployments, ['id', deploymentId]);
    this.modalService.open(content);
  }

  showDateChange(content, deploymentId: number) {
    this.deployment = _.find(this.deployments, ['id', deploymentId]);
    this.deploymentDate = DateTimeModel.fromEpoch(this.deployment.deploymentDate);
    this.modalService.open(content).result.then(
      () => {
        this.deployment.deploymentDate = this.deploymentDate.toEpoch();
        this.editDeploymentDate.emit(this.deployment);
        delete this.deploymentDate;
      },
      () => {
        delete this.deploymentDate;
      },
    );
  }

  showConfirm(content, deploymentId: number) {
    this.deployment = _.find(this.deployments, ['id', deploymentId]);

    this.modalService.open(content).result.then(
      () => {
        this.doConfirmDeployment.emit(this.deployment);
      },
      () => {},
    );
  }

  showReject(content, deploymentId: number) {
    this.deployment = _.find(this.deployments, ['id', deploymentId]);
    this.modalService.open(content).result.then(
      () => {
        this.doRejectDeployment.emit(this.deployment);
      },
      () => {},
    );
  }

  showCancel(content, deploymentId: number) {
    this.deployment = _.find(this.deployments, ['id', deploymentId]);
    this.modalService.open(content).result.then(
      () => {
        this.doCancelDeployment.emit(this.deployment);
      },
      () => {},
    );
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
      /* happy path */ (r) => {
        if (r) {
          window.location.href = '/AMW_web/pages/editResourceView.xhtml?id=' + appId + '&ctx=1';
        }
      },
    );
  }
}
