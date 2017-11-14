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
  autoload: boolean = true;

  // value of filters parameter. Used to pass as json object to the logView.xhtml
  filtersInUrl: DeploymentFilter[];

  // valid for all, loaded once
  filterTypes: DeploymentFilterType[] = [];
  comparatorOptions: ComparatorFilterOption[] = [];
  comparatorOptionsMap: { [key: string]: string } = {};
  hasPermissionToRequestDeployments: boolean = false;
  csvSeparator: string = '';

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
  filterString: string;

  // filtered deployments
  deployments: Deployment[] = [];

  // csv export
  deploymentDetailMap: { [key: number]: DeploymentDetail } = {};
  csvReadyObjects: any[] = [];
  csvDocument: string;

  // sorting with default values
  sortCol: string = 'd.deploymentDate';
  sortDirection: string = 'DESC';

  // pagination with default values
  maxResults: number = 10;
  offset: number = 0;
  allResults: number;
  currentPage: number;
  lastPage: number;

  errorMessage: string = '';
  successMessage: string = '';
  isLoading: boolean = true;

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

    this.activatedRoute.queryParams.subscribe(
      (param: any) => {
        if (param['filters']) {
          try {
            this.paramFilters = JSON.parse(param['filters']);
          } catch (e) {
            console.error(e);
            this.errorMessage = 'Error parsing filter';
            this.autoload = false;
          }
        } else {
          if (sessionStorage.getItem('deploymentFilters')) {
            this.paramFilters = JSON.parse(sessionStorage.getItem('deploymentFilters'));
          }
        }
        this.initTypeAndOptions();
        this.canRequestDeployments();
        this.getCsvSeparator();
    });
  }

  addFilter() {
    if (this.selectedFilterType && this.canFilterBeAdded()) {
      let newFilter: DeploymentFilter = <DeploymentFilter> {};
      newFilter.name = this.selectedFilterType.name;
      newFilter.comp = this.defaultComparator;
      newFilter.val = this.selectedFilterType.type === 'booleanType' ? 'true' : '';
      newFilter.type = this.selectedFilterType.type;
      newFilter.compOptions = this.comparatorOptionsForType(this.selectedFilterType.type);
      this.setValueOptionsForFilter(newFilter);
      this.filters.unshift(newFilter);
      this.enableDatepicker(newFilter.type);
      this.offset = 0;
    }
  }

  removeFilter(filter: DeploymentFilter) {
    let i: number = _.findIndex(this.filters, {name: filter.name, comp: filter.comp, val: filter.val});
    if (i !== -1) {
      this.filters.splice(i, 1);
    }
    this.offset = 0;
  }

  clearFilters() {
    this.filters = [];
    this.filterString = null;
  }

  applyFilter() {
    let filtersForBackend: DeploymentFilter[] = [];
    let filtersForParam: DeploymentFilter[] = [];
    let filtersToBeRemoved: DeploymentFilter[] = [];
    this.errorMessage = '';
    this.filters.forEach((filter) => {
      if (filter.val || filter.type === 'SpecialFilterType') {
        filtersForParam.push(<DeploymentFilter> {name: filter.name, comp: filter.comp, val: filter.val});
        if (filter.type === 'DateType') {
          let dateTime = moment(filter.val, 'DD.MM.YYYY HH:mm');
          if (!dateTime || !dateTime.isValid()) {
            this.errorMessage = 'Invalid date';
          }
          filtersForBackend.push(<DeploymentFilter> {
            name: filter.name,
            comp: filter.comp,
            val: dateTime.valueOf().toString()
          });
        } else {
          filtersForBackend.push(<DeploymentFilter> {name: filter.name, comp: filter.comp, val: filter.val});
        }
      } else {
        filtersToBeRemoved.push(filter);
      }
    });
    filtersToBeRemoved.forEach((filter) => this.removeFilter(filter));

    if (!this.errorMessage) {
      this.getFilteredDeployments(JSON.stringify(filtersForBackend));
      this.filtersInUrl = filtersForParam;
      let filterString: string;
      if (this.filtersInUrl.length > 0) {
        filterString = JSON.stringify(this.filtersInUrl);
        sessionStorage.setItem('deploymentFilters', filterString);
      }
      this.filterString = filterString;
    }
  }

  changeDeploymentDate(deployment: Deployment) {
    if (deployment) {
      this.setDeploymentDate(deployment, deployment.deploymentDate);
    }
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
        /* onComplete */  () => $('#deploymentsEdit').modal('show')
      );
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

  confirmDeployment(deploymentDetail: DeploymentDetail) {
    if (deploymentDetail) {
      this.deploymentService.confirmDeployment(deploymentDetail).subscribe(
        /* happy path */ (r) => r,
        /* error path */ (e) => this.errorMessage = this.errorMessage ? this.errorMessage + '<br>' + e : e,
        /* onComplete */ () => this.reloadDeployment(deploymentDetail.deploymentId)
      );
    }
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
    this.errorMessage = 'Generating your CSV.<br>Please hold on, depending on the requested data this may take a while';
    this.deployments.forEach((deployment) => {
      this.getDeploymentDetailForCsvExport(deployment);
    });
  }

  copyURL() {
    let url: string = decodeURIComponent(window.location.href);
    let i: number = url.indexOf('?');
    if (i > 0) {
      url = url.substring(0, i);
    }
    url += '?filters=' + this.filterString;
    $("body").append($('<input type="text" name="fname" class="textToCopyInput" style="opacity:0"/>')
      .val(url)).find(".textToCopyInput").select();
    try {
      document.execCommand('copy');
    } catch (e) {
      window.prompt("Press Ctrl + C, then Enter to copy to clipboard", url);
    }
    $(".textToCopyInput").remove();
  }

  sortDeploymentsBy(col: string) {
    if (this.sortCol === col) {
      this.sortDirection = this.sortDirection === 'DESC' ? 'ASC' : 'DESC';
    } else {
      this.sortCol = col;
      this.sortDirection = 'DESC';
    }
    this.applyFilter();
  }

  setMaxResultsPerPage(max: number) {
    this.maxResults = max;
    this.offset = 0;
    this.applyFilter();
  }

  setNewOffset(offset: number) {
    this.offset = offset;
    this.applyFilter();
  }

  private canFilterBeAdded(): boolean {
    return this.selectedFilterType.name !== 'Latest deployment job for App Server and Env' ||
      _.findIndex(this.filters, {name: this.selectedFilterType.name}) === -1;
  }

  private populateCSVrows(deployment: Deployment) {
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
      this.errorMessage = '';
    }
  }

  private createCSV(): string {
    let content: string = this.createCSVTitles();
    this.csvReadyObjects.forEach((deployment) => {
      let line: string = '';
      for (const field of Object.keys(deployment)) {
        switch (field) {
          case 'id':
          case 'trackingId':
            line += deployment[field].toString() + this.csvSeparator;
            break;
          case 'deploymentDate':
          case 'deploymentJobCreationDate':
          case 'deploymentConfirmationDate':
          case 'deploymentCancelDate':
          case 'stateToDeploy':
            line += deployment[field] ? '"' + moment(deployment[field]).format('YYYY-MM-DD HH:mm').toString() + '"' + this.csvSeparator : this.csvSeparator;
            break;
          case 'appsWithVersion':
            deployment[field].forEach((appsWithVersion) => {
              line += '"' + appsWithVersion['applicationName'] + ' ' + appsWithVersion['version'] + '\n"';
            });
            line += this.csvSeparator;
            break;
          case 'deploymentParameters':
            deployment[field].forEach((deploymentParameter) => {
              line += '"' + deploymentParameter['key'] + ' ' + deploymentParameter['value'] + '\n"';
            });
            line += this.csvSeparator;
            break;
          default:
            line += deployment[field] !== null ? '"' + deployment[field] + '"' + this.csvSeparator : this.csvSeparator;
            break;
        }
      }
      content += line.slice(0, -1) + '\n';
    });
    return content;
  }

  private createCSVTitles(): string {
    let labelsArray: string[]  = [
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
    return labelsArray.join(this.csvSeparator) + '\n';
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
      /* on complete */ () => this.populateCSVrows(deployment)
    );
  }

  private getCsvSeparator() {
    this.deploymentService.getCsvSeparator().subscribe(
      /* happy path */ (r) => this.csvSeparator = r,
      /* error path */ (e) => this.errorMessage = e);
  }

  private confirmSelectedDeployments() {
    this.deployments.filter((deployment) => deployment.selected === true).forEach((deployment) => {
      this.deploymentService.getDeploymentDetail(deployment.id).subscribe(
        /* happy path */ (r) => this.deploymentDetailMap[deployment.id] = r,
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

  private applyConfirmationAttributesIntoDeploymentDetailAndDoConfirm(deploymentId: number) {
    let deploymentDetail = this.deploymentDetailMap[deploymentId];
    deploymentDetail.sendEmailWhenDeployed = this.confirmationAttributes.sendEmailWhenDeployed;
    deploymentDetail.simulateBeforeDeployment = this.confirmationAttributes.simulateBeforeDeployment;
    deploymentDetail.shakedownTestsWhenDeployed = this.confirmationAttributes.shakedownTestsWhenDeployed;
    deploymentDetail.neighbourhoodTest = this.confirmationAttributes.neighbourhoodTest;
    this.confirmDeployment(deploymentDetail);
  }

  private setSelectedDeploymentDates() {
    let dateTime = moment(this.deploymentDate, 'DD.MM.YYYY HH:mm');
    if (!dateTime || !dateTime.isValid()) {
      this.errorMessage = 'Invalid date';
    } else {
      _.filter(this.deployments, {selected: true}).forEach((deployment) => this.setDeploymentDate(deployment, dateTime.valueOf()));
    }
  }

  private setDeploymentDate(deployment: Deployment, deploymentDate: number) {
    this.deploymentService.setDeploymentDate(deployment.id, deploymentDate).subscribe(
      /* happy path */ (r) => r,
      /* error path */ (e) => this.errorMessage = this.errorMessage ? this.errorMessage + '<br>' + e : e,
      /* on complete */ () => this.reloadDeployment(deployment.id)
    );
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
    if (!this.filterValueOptions[filter.name]) {
      if (filter.type === 'booleanType') {
        filter.valOptions = this.filterValueOptions[filter.name] = [ 'true', 'false' ];
      } else {
        this.getAndSetFilterOptionValues(filter);
      }
    }
    filter.valOptions = this.filterValueOptions[filter.name];
  }

  private mapStates() {
    if (this.deployments) {
      this.deployments.forEach((deployment) => {
        switch (deployment.state) {
          case 'PRE_DEPLOYMENT':
            deployment.state = 'pre_deploy';
            break;
          case 'READY_FOR_DEPLOYMENT':
            deployment.state = 'ready_for_deploy';
            break;
          default:
            break;
        }
      });
    }
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
                               this.enhanceParamFilter(); }
    );
  }

  private getAndSetFilterOptionValues(filter: DeploymentFilter) {
    this.deploymentService.getFilterOptionValues(filter.name).subscribe(
      /* happy path */ (r) => this.filterValueOptions[filter.name] = r,
      /* error path */ (e) => this.errorMessage = e,
      /* onComplete */ () => filter.valOptions = this.filterValueOptions[filter.name]);
  }

  private getFilteredDeployments(filterString: string) {
    this.isLoading = true;
    this.deploymentService.getFilteredDeployments(filterString, this.sortCol, this.sortDirection, this.offset, this.maxResults).subscribe(
      /* happy path */ (r) => { this.deployments = r.deployments;
                                this.allResults = r.total;
                                this.currentPage = Math.floor(this.offset / this.maxResults) + 1;
                                this.lastPage = Math.ceil(this.allResults / this.maxResults); },
      /* error path */ (e) => { this.errorMessage = e;
                                this.isLoading = false; },
      /* onComplete */ () => { this.isLoading = false;
                               this.mapStates(); }
    );
  }

  private canRequestDeployments() {
      this.deploymentService.canRequestDeployments().subscribe(
        /* happy path */ (r) => this.hasPermissionToRequestDeployments = r,
        /* error path */ (e) => this.errorMessage = e);
  }

  private enhanceParamFilter() {
    if (this.paramFilters) {
      this.clearFilters();
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
    if (this.autoload) {
      this.applyFilter();
    }
  }

  private populateMap() {
    this.comparatorOptions.forEach((option) => {
      this.comparatorOptionsMap[option.name] = option.displayName;
    });
    this.isLoading = false;
  }

}
