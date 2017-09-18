import { Component, OnInit, NgZone } from '@angular/core';
import { Location } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import { AppState } from '../app.service';
import { ComparatorFilterOption } from './comparator-filter-option';
import { Deployment } from './deployment';
import { DeploymentDetail } from './deployment-detail';
import { DeploymentFilter } from './deployment-filter';
import { DeploymentFilterType } from './deployment-filter-type';
import { DeploymentService } from './deployment.service';
import { ResourceService } from '../resource/resource.service';
import { Datetimepicker } from 'eonasdan-bootstrap-datetimepicker';
import * as _ from 'lodash';
import * as moment from 'moment';

declare var $: any;

@Component({
  selector: 'amw-deployments',
  templateUrl: './deployments.component.html'
})

export class DeploymentsComponent implements OnInit {

  defaultComparator: string = 'eq';

  // initially by queryParam
  paramFilters: DeploymentFilter[] = [];

  // valid for all, loaded once
  filterTypes: DeploymentFilterType[] = [];
  comparatorOptions: ComparatorFilterOption[] = [];
  comparatorOptionsMap: { [key: string]: string } = {};
  hasPermissionToRequestDeployments: boolean = false;

  // available edit actions
  editActions: string[] = ['Change date', 'Confirm', 'Reject', 'Cancel'];
  selectedEditAction: string = this.editActions[0];
  // confirmation dialog / edit multiple deployments
  hasPermissionShakedownTest: boolean = false;
  deploymentDate: number; // for deployment date change
  confirmationAttributes: DeploymentDetail;

  // available filterValues (if any)
  filterValueOptions: { [key: string]: string[] } = {};

  // to be added
  selectedFilterType: DeploymentFilterType;

  // already set
  filters: DeploymentFilter[] = [];

  // filtered deployments
  deployments: Deployment[] = [];

  // csv export
  deploymentDetailMap: { [key: number]: DeploymentDetail } = {};
  csvReadyObjects: any[] = [];
  csvDocument: string;

  errorMessage: string = '';
  successMessage: string = '';
  isLoading: boolean = false;

  constructor(private activatedRoute: ActivatedRoute,
              private ngZone: NgZone,
              private location: Location,
              private deploymentService: DeploymentService,
              private resourceService: ResourceService,
              public appState: AppState) {
  }

  ngOnInit() {

    this.appState.set('navShow', false);
    this.appState.set('navTitle', 'Deployments');
    this.appState.set('pageTitle', 'Deployments');

    console.log('hello `Deployments` component');

    this.activatedRoute.queryParams.subscribe(
      (param: any) => {
        if (param['filters']) {
          try {
            this.paramFilters = JSON.parse(param['filters']);
          } catch (e) {
            console.error(e);
            this.errorMessage = 'Error parsing filter';
          }
        }
    });

    this.initTypeAndOptions();
    this.canRequestDeployments();

  }

  addFilter() {
    if (this.selectedFilterType) {
      let newFilter: DeploymentFilter = <DeploymentFilter> {};
      newFilter.name = this.selectedFilterType.name;
      newFilter.comp = this.defaultComparator;
      newFilter.val = this.selectedFilterType.type === 'booleanType' ? 'true' : '';
      newFilter.type = this.selectedFilterType.type;
      newFilter.compOptions = this.comparatorOptionsForType(this.selectedFilterType.type);
      this.setValueOptionsForFilter(newFilter);
      this.filters.unshift(newFilter);
      this.enableDatepicker(newFilter.type);
    }
  }

  removeFilter(filter: DeploymentFilter) {
    _.remove(this.filters, {name: filter.name, comp: filter.comp, val: filter.val});
  }

  clearFilters() {
    this.filters = [];
  }

  applyFilter() {
    let filtersForBackend: DeploymentFilter[] = [];
    let filtersForParam: DeploymentFilter[] = [];
    this.errorMessage = '';
    this.filters.forEach((filter) => {
      filtersForParam.push(<DeploymentFilter> {name: filter.name, comp: filter.comp, val: filter.val});
      if (filter.type === 'DateType') {
        let dateTime = moment(filter.val, 'DD.MM.YYYY HH:mm');
        if (!dateTime || !dateTime.isValid()) {
          this.errorMessage = 'Invalid date';
        }
        filtersForBackend.push(<DeploymentFilter> {name: filter.name, comp: filter.comp, val: dateTime.valueOf().toString()});
      } else {
        filtersForBackend.push(<DeploymentFilter> {name: filter.name, comp: filter.comp, val: filter.val});
      }
    });
    if (!this.errorMessage) {
      this.getFilteredDeployments(JSON.stringify(filtersForBackend));
      this.goTo(JSON.stringify(filtersForParam));
    }
  }

  changeDeploymentDate(deployment: Deployment) {
    if (deployment) {
      this.setDeploymentDate(deployment, deployment.deploymentDate);
    }
  }

  setDeploymentDate(deployment: Deployment, deploymentDate: number) {
    this.deploymentService.setDeploymentDate(deployment.id, deploymentDate).subscribe(
      /* happy path */ (r) => r,
      /* error path */ (e) => this.errorMessage = this.errorMessage ? this.errorMessage + '<br>' + e : e,
      /* on complete */ () => this.reloadDeployment(deployment.id)
    );
  }

  changeEditAction() {
    if (this.selectedEditAction === 'Change date') {
      this.addDatePicker();
    }
  }

  switchDeployments(enable: boolean) {
    this.deployments.forEach((deployment) => deployment.selected = enable);
  }

  editableDeployments(): boolean {
    return (_.findIndex(this.deployments, {selected: true}) !== -1);
  }

  showEdit() {
    if (this.editableDeployments()) {
      this.addDatePicker();
      this.confirmationAttributes = <DeploymentDetail> {};
      // get shakeDownTestPermission for first element
      let indexOfFirstSelectedElem = _.findIndex(this.deployments, {selected: true});
      let firstDeployment = this.deployments[indexOfFirstSelectedElem];
      this.resourceService.canCreateShakedownTest(firstDeployment.appServerId).subscribe(
        /* happy path */ (r) => this.hasPermissionShakedownTest = r,
        /* error path */ (e) => this.errorMessage = e,
        /* onComplete */  () => $('#deploymentsEdit').modal('show'));
    }
  }

  doEdit() {
    if (this.editableDeployments()) {
      this.errorMessage = '';
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

  private confirmSelectedDeployments() {
    this.deployments.filter(deployment => deployment.selected === true).forEach((deployment) => {
      this.deploymentService.getDeploymentDetail(deployment.id).subscribe(
        /* happy path */ (r) => this.deploymentDetailMap[deployment.id] = r,
        /* error path */ (e) => this.errorMessage = this.errorMessage ? this.errorMessage + '<br>' + e : e,
        /* on complete */ () => this.applyConfirmationAttributesIntoDeploymentDetailAndDoConfirm(deployment.id)
      );
    });
  }

  private rejectSelectedDeployments() {
    this.deployments.filter(deployment => deployment.selected === true).forEach( (deployment) => {
      this.rejectDeployment(deployment)
    });
  }

  private cancelSelectedDeployments() {
    let selectedDeployments =  this.deployments.filter(deployment => deployment.selected === true);
    selectedDeployments.forEach( (deployment) => {
      this.cancelDeployment(deployment)
    });
  }

  private applyConfirmationAttributesIntoDeploymentDetailAndDoConfirm(deploymentId: number) {
    let deploymentDetail = this.deploymentDetailMap[deploymentId];
    deploymentDetail.sendEmailWhenDeployed = this.confirmationAttributes.sendEmailWhenDeployed;
    deploymentDetail.simulateBeforeDeployment = this.confirmationAttributes.simulateBeforeDeployment;
    deploymentDetail.shakedownTestsWhenDeployed = this.confirmationAttributes.shakedownTestsWhenDeployed;
    deploymentDetail.neighbourhoodTest = this.confirmationAttributes.neighbourhoodTest;
    this.confirmDeployment(deploymentDetail);
  }

  cancelDeployment(deployment: Deployment) {
    if (deployment) {
      this.deploymentService.cancelDeployment(deployment.id).subscribe(
        /* happy path */ (r) => r,
        /* error path */ (e) => this.errorMessage = this.errorMessage ? this.errorMessage + '<br>' + e : e,
        /* onComplete */ () => this.reloadDeployment(deployment.id)
      );
    }
  }

  exportCSV() {
    this.isLoading = true;
    this.deployments.forEach((deployment) => {
      this.getDeploymentDetailForCsvExport(deployment);
    });
  }

  private populateCsvRows(deployment: Deployment) {
    let detail: DeploymentDetail = this.deploymentDetailMap[deployment.id];
    let csvReadyObject: any = {
      id: deployment['id'],
      trackingId: deployment['trackingId'],
      state: deployment['state'],
      buildSuccess: detail.buildSuccess,
      executed: detail.executed,
      appServerName: deployment['appServerName'],
      appsWithVersion: deployment['appsWithVersion'],
      releaseName: deployment['releaseName'],
      environmentName: deployment['environmentName'],
      runtimeName: deployment['runtimeName'],
      deploymentParameters: deployment['deploymentParameters'],
      deploymentJobCreationDate: deployment['deploymentJobCreationDate'],
      requestUser: deployment['requestUser'],
      deploymentDate: deployment['deploymentDate'],
      stateToDeploy: detail.stateToDeploy,
      deploymentConfirmed: 'Deployment confirmed',
      deploymentConfirmationDate: deployment['deploymentConfirmationDate'],
      confirmUser: deployment['confirmUser'],
      deploymentCancelDate: deployment['deploymentCancelDate'],
      cancelUser: deployment['cancelUser'],
      stateMessage: detail.stateMessage
    };
    this.csvReadyObjects.push(csvReadyObject);
    if (this.csvReadyObjects.length === this.deployments.length) {
      this.csvDocument = this.createCSV();
      let docName: string = 'deployments_' + moment().format('YYYY-MM-DD_HHmm').toString() + '.csv';
      this.pushDownload(docName);
    }
  }

  private createCSV(): string {
    let labels: string = '';
    let content: string = '';
    let labelsMap: string[]  = [
      'Id',
      'Tracking Id',
      'Deployment state',
      'Build success',
      'Deployment executed',
      'App server',
      'Applications',
      'Deployment release',
      'Environment',
      'Target platform',
      'Deployment parameters',
      'Creation date',
      'Request user',
      'Deployment date',
      'Configuration to deploy',
      'Deployment confirmed',
      'Confirmation date',
      'Confirmation user',
      'Cancel date',
      'Cancel user',
      'Status message'
    ];

    labelsMap.forEach((label) => {
      labels += label + ',';
    });
    content += labels.slice(0, -1) + '\n';

    this.csvReadyObjects.forEach((deployment) => {
      let line: string = '';
      for (const field of Object.keys(deployment)) {
        switch (field) {
          case 'id':
          case 'trackingId':
            line += deployment[field].toString() + ',';
            break;
          case 'deploymentDate':
          case 'deploymentJobCreationDate':
          case 'deploymentConfirmationDate':
          case 'deploymentCancelDate':
          case 'stateToDeploy':
            line += deployment[field] ? '"' + moment(deployment[field]).format('YYYY-MM-DD HH:mm').toString() + '",' : ',';
            break;
          case 'appsWithVersion':
            deployment[field].forEach((appsWithVersion) => {
              line += '"' + appsWithVersion['applicationName'] + ' ' + appsWithVersion['version'] + '\n"';
            });
            line += ',';
            break;
          case 'deploymentParameters':
            deployment[field].forEach((deploymentParameter) => {
              line += '"' + deploymentParameter['key'] + ' ' + deploymentParameter['value'] + '\n"';
            });
            line += ',';
            break;
          default:
            line += deployment[field] !== null ? '"' + deployment[field] + '",' : ',';
            break;
        }

      }
      line = line.slice(0, -1);
      content += line + '\n';
    });
    return content;
  }

  private pushDownload(docName: string) {
    this.isLoading = false;
    let blob = new Blob([this.csvDocument], { type: 'text/csv' });
    let url = window.URL.createObjectURL(blob);
    if (navigator.msSaveOrOpenBlob) {
      navigator.msSaveBlob(blob, docName);
    } else {
      let a = document.createElement('a');
      a.href = url;
      a.download = docName;
      a.setAttribute('style', 'display:none;');
      document.body.appendChild(a);
      a.click();
      document.body.removeChild(a);
    }
    window.URL.revokeObjectURL(url);
  }

  private getDeploymentDetailForCsvExport(deployment: Deployment) {
    this.deploymentService.getDeploymentDetail(deployment.id).subscribe(
      /* happy path */ (r) => this.deploymentDetailMap[deployment.id] = r,
      /* error path */ (e) => this.errorMessage = e,
      /* on complete */ () => this.populateCsvRows(deployment)
    );
  }


  rejectDeployment(deployment: Deployment) {
    if (deployment) {
      this.deploymentService.rejectDeployment(deployment.id).subscribe(
        /* happy path */ (r) => r,
        /* error path */ (e) => this.errorMessage = this.errorMessage ? this.errorMessage + '<br>' + e : e,
        /* onComplete */ () => this.reloadDeployment(deployment.id)
      );
    }
  }

  confirmDeployment(deploymentDetail: DeploymentDetail) {
    if (deploymentDetail) {
      this.deploymentService.confirmDeployment(deploymentDetail).subscribe(
        /* happy path */ (r) => r,
        /* error path */ (e) => this.errorMessage = e,
        /* onComplete */  () => this.reloadDeployment(deploymentDetail.deploymentId)
      );
    }
  }

  private setSelectedDeploymentDates() {
    let dateTime = moment(this.deploymentDate, 'DD.MM.YYYY HH:mm');
    if (!dateTime || !dateTime.isValid()) {
      this.errorMessage = 'Invalid date';
    } else {
      _.filter(this.deployments, {selected: true}).forEach((deployment) => this.setDeploymentDate(deployment, dateTime.valueOf()));
    }
  }

  private reloadDeployment(deploymentId: number) {
    let reloadedDeployment: Deployment;
    this.deploymentService.getWithActions(deploymentId).subscribe(
      /* happy path */ (r) => reloadedDeployment = r,
      /* error path */ (e) => this.errorMessage = e,
      /* on complete */ () => this.updateDeploymentsList(reloadedDeployment)
    );
  }

  private updateDeploymentsList(deployment: Deployment) {
    this.deployments.splice(_.findIndex(this.deployments, {id: deployment.id}), 1, deployment);
  }

  private enableDatepicker(filterType: string) {
    if (filterType === 'DateType') {
      this.addDatePicker();
    }
  }

  private addDatePicker() {
    this.ngZone.onMicrotaskEmpty.first().subscribe(() => {
      $('.datepicker').datetimepicker({format: 'DD.MM.YYYY HH:mm'});
    });
  }

  private comparatorOptionsForType(filterType: string) {
    if (filterType === 'booleanType' || filterType === 'StringType' || filterType === 'ENUM_TYPE') {
      return [{name: 'eq', displayName: 'is'}];
    } else {
      return this.comparatorOptions;
    }
  }

  private setValueOptionsForFilter(filter: DeploymentFilter) {
    console.log('valueOptionsForFilter ' + filter.name + ', ' + filter.type);
    if (!this.filterValueOptions[filter.name]) {
      if (filter.type === 'booleanType') {
        filter.valOptions = this.filterValueOptions[filter.name] = [ 'true', 'false' ];
      } else {
        this.getAndSetFilterOptionValues(filter);
      }
    }
    filter.valOptions = this.filterValueOptions[filter.name];
  }

  private initTypeAndOptions() {
    this.isLoading = true;
    this.deploymentService.getAllDeploymentFilterTypes().subscribe(
      /* happy path */ (r) => this.filterTypes = _.sortBy(r, 'name'),
      /* error path */ (e) => this.errorMessage = e,
      /* onComplete */ () => this.getAllComparatorOptions());
  }

  private getAllComparatorOptions() {
    this.deploymentService.getAllComparatorFilterOptions().subscribe(
      /* happy path */ (r) => this.comparatorOptions = r,
      /* error path */ (e) => this.errorMessage = e,
      /* onComplete */ () => { this.populateMap();
                               this.enhanceParamFilter();
      });
  }

  private getAndSetFilterOptionValues(filter: DeploymentFilter) {
    this.deploymentService.getFilterOptionValues(filter.name).subscribe(
      /* happy path */ (r) => this.filterValueOptions[filter.name] = r,
      /* error path */ (e) => this.errorMessage = e,
      /* onComplete */ () => filter.valOptions = this.filterValueOptions[filter.name]);
  }

  private getFilteredDeployments(filterString: string) {
    this.isLoading = true;
    this.deploymentService.getFilteredDeployments(filterString).subscribe(
      /* happy path */ (r) => this.deployments = r,
      /* error path */ (e) => { this.errorMessage = e;
                                this.isLoading = false; },
      /* onComplete */ () => this.isLoading = false);
  }

  private canRequestDeployments() {
      this.deploymentService.canRequestDeployments().subscribe(
        /* happy path */ (r) => this.hasPermissionToRequestDeployments = r,
        /* error path */ (e) => this.errorMessage = e);
  }

  private enhanceParamFilter() {
    if (this.paramFilters) {
      this.paramFilters.forEach((filter) => {
        let i: number = _.findIndex(this.filterTypes, ['name', filter.name]);
        if (i >= 0) {
          filter.type = this.filterTypes[i].type;
          filter.compOptions = this.comparatorOptionsForType(filter.type);
          filter.comp = !filter.comp ? this.defaultComparator : filter.comp;
          this.setValueOptionsForFilter(filter);
          this.filters.push(filter);
          this.enableDatepicker(filter.type);
        } else {
          this.errorMessage = 'Error parsing filter';
        }
      });
    }
    this.isLoading = false;
  }

  private populateMap() {
    this.comparatorOptions.forEach((option) => {
      this.comparatorOptionsMap[option.name] = option.displayName;
    });
    this.isLoading = false;
  }

  private goTo(destination: string) {
    this.location.go('/deployments?filters=' + destination);
  }

}
