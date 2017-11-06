import { Component, Input, Output, EventEmitter, NgZone } from '@angular/core';
import { ResourceService } from '../resource/resource.service';
import { Deployment } from './deployment';
import { DeploymentDetail } from './deployment-detail';
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
  @Input() filtersInUrl: DeploymentFilter[];
  @Input() sortCol: string;
  @Input() sortDirection: string;
  @Input() currentPage: number;
  @Input() lastPage: number;
  @Output() editDeploymentDate: EventEmitter<Deployment> = new EventEmitter<Deployment>();
  @Output() selectAllDeployments: EventEmitter<boolean> = new EventEmitter<boolean>();
  @Output() doCancelDeployment: EventEmitter<Deployment> = new EventEmitter<Deployment>();
  @Output() doRejectDeployment: EventEmitter<Deployment> = new EventEmitter<Deployment>();
  @Output() doConfirmDeployment: EventEmitter<DeploymentDetail> = new EventEmitter<DeploymentDetail>();
  @Output() doSort: EventEmitter<string> = new EventEmitter<string>();
  @Output() doSetMax: EventEmitter<number> = new EventEmitter<number>();
  @Output() doSetOffset: EventEmitter<number> = new EventEmitter<number>();

  deployment: Deployment;

  deploymentDate: number;

  deploymentDetail: DeploymentDetail;

  hasPermissionShakedownTest: boolean = null;

  errorMessage: string = '';

  allSelected: boolean = false;

  maxResults: number = 10;

  paginatorItems: number = 5;

  failureReason: { [key: string]: string } = { 'PRE_DEPLOYMENT_GENERATION': 'pre deployment generation',
    'DEPLOYMENT_GENERATION': 'deployment generation', 'PRE_DEPLOYMENT_SCRIPT': 'pre deployment script',
    'DEPLOYMENT_SCRIPT': 're deployment script', 'NODE_MISSING': 'node missing', 'TIMEOUT': 'timeout',
    'UNEXPECTED_ERROR': 'unexpected error', 'RUNTIME_ERROR': 'runtime error' };

  constructor(private ngZone: NgZone,
              private deploymentService: DeploymentService,
              private resourceService: ResourceService) {
  }

  pages(): number[] {
    if (this.lastPage > 1) {
      let itemsBefore: number = Math.floor(this.paginatorItems/2);
      let start: number = this.currentPage > itemsBefore ? this.currentPage-itemsBefore : 1;
      let end: number = start + this.paginatorItems - 1;
      return this.range(start, end < this.lastPage ? end : this.lastPage);
    }
    return;
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
    delete this.deploymentDetail;
    this.deployment = _.find(this.deployments, ['id', deploymentId]);
    this.resourceService.canCreateShakedownTest(this.deployment.appServerId).subscribe(
      /* happy path */ (r) => this.hasPermissionShakedownTest = r,
      /* error path */ (e) => this.errorMessage = e,
      /* onComplete */  () => this.getDeploymentDetailsAndShowConfirmationModal(deploymentId));
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

  doConfirm() {
    if (this.deployment && this.deploymentDetail) {
      this.doConfirmDeployment.emit(this.deploymentDetail);
      $('#deploymentConfirmation').modal('hide');
      delete this.deployment;
      delete this.deploymentDetail;
    }
  }

  reSort(col: string) {
    this.doSort.emit(col);
  }

  setMax() {
    this.doSetMax.emit(this.maxResults);
  }

  toPage(page: number) {
    if (page <= this.lastPage && page != this.currentPage) {
      page = page > 0 ? page -1 : 0;
      this.doSetOffset.emit(page*this.maxResults);
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

  private getDeploymentDetailsAndShowConfirmationModal(deploymentId: number) {
    this.deploymentService.getDeploymentDetail(deploymentId).subscribe(
      /* happy path */ (r) => this.deploymentDetail = r,
      /* error path */ (e) => this.errorMessage = e,
      /* onComplete */  () => $('#deploymentConfirmation').modal('show'));
  }

  private range(a: number, b: number) {
    let d: number [] = [];
    let c: number = b-a+1;
    while(c--) {
      d[c]=b--;
    }
    return d;
  }

}
