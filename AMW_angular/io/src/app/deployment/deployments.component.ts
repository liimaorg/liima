import { Component, OnInit, NgZone } from '@angular/core';
import { Location } from '@angular/common';
import { NgModel } from '@angular/forms';
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

  // enhanced filters for deployment service
  filtersForBackend: DeploymentFilter[] = [];
  // value of filters parameter. Used to pass as json object to the logView.xhtml
  filtersForParam: DeploymentFilter[] = [];

  // valid for all, loaded once
  filterTypes: DeploymentFilterType[] = [];
  comparatorOptions: ComparatorFilterOption[] = [];
  comparatorOptionsMap: { [key: string]: string } = {};
  hasPermissionToRequestDeployments: boolean = false;
  csvSeparator: string = '';

  // available edit actions
  editActions: string[] = ['Change date', 'Confirm', 'Reject', 'Cancel'];
  hasPermissionShakedownTest: boolean = false;
  deploymentDate: number; // for deployment date change

  // available filterValues (if any)
  filterValueOptions: { [key: string]: string[] } = {};

  // to be added
  selectedFilterType: DeploymentFilterType;

  // already set
  filters: DeploymentFilter[] = [];

  // filtered deployments
  deployments: Deployment[] = [];

  // csv export
  deploymentsForExport: Deployment[] = [];
  deploymentDetailMap: { [key: number]: DeploymentDetail } = {};
  csvReadyObjects: { [key: number]: any[] } = {};
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

  @ViewChild('selectModel')
  selectModel: NgModel;

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
      const newFilter: DeploymentFilter = {} as DeploymentFilter;
      newFilter.name = this.selectedFilterType.name;
      newFilter.comp = this.defaultComparator;
      newFilter.val = this.selectedFilterType.type === 'booleanType' ? 'true' : '';
      newFilter.type = this.selectedFilterType.type;
      newFilter.compOptions = this.comparatorOptionsForType(this.selectedFilterType.type);
      this.setValueOptionsForFilter(newFilter);
      this.filters.push(newFilter);
      this.enableDatepicker(newFilter.type);
      this.offset = 0;
      this.selectedFilterType = null;
      this.selectModel.reset(null);
    }
  }

  removeFilter(filter: DeploymentFilter) {
    const i: number = _.findIndex(this.filters, {name: filter.name, comp: filter.comp, val: filter.val});
    if (i !== -1) {
      this.filters.splice(i, 1);
    }
    this.offset = 0;
  }

  clearFilters() {
    this.filters = [];
    sessionStorage.setItem('deploymentFilters', null);
    this.updateFiltersInURL(null);
  }

  applyFilters() {
    this.filtersForBackend = [];
    this.filtersForParam = [];
    const filtersToBeRemoved: DeploymentFilter[] = [];
    this.errorMessage = '';
    this.filters.forEach((filter) => {
      if (filter.val || filter.type === 'SpecialFilterType') {
        this.filtersForParam.push({name: filter.name, comp: filter.comp, val: filter.val} as DeploymentFilter);
        if (filter.type === 'DateType') {
          const dateTime = moment(filter.val, 'DD.MM.YYYY HH:mm');
          if (!dateTime || !dateTime.isValid()) {
            this.errorMessage = 'Invalid date';
          }
          this.filtersForBackend.push({name: filter.name, comp: filter.comp, val: dateTime.valueOf().toString()} as DeploymentFilter);
        } else {
          this.filtersForBackend.push({name: filter.name, comp: filter.comp, val: filter.val} as DeploymentFilter);
        }
      } else {
        filtersToBeRemoved.push(filter);
      }
    });
    filtersToBeRemoved.forEach((filter) => this.removeFilter(filter));

    if (!this.errorMessage) {
      this.getFilteredDeployments(JSON.stringify(this.filtersForBackend));
      let filterString: string = null;
      if (this.filtersForParam.length > 0) {
        filterString = JSON.stringify(this.filtersForParam);
      }
      sessionStorage.setItem('deploymentFilters', filterString);
      this.updateFiltersInURL(filterString);
    }
  }

  changeDeploymentDate(deployment: Deployment) {
    if (deployment) {
      this.setDeploymentDate(deployment, deployment.deploymentDate);
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
      // get shakeDownTestPermission for first element
      const indexOfFirstSelectedElem = _.findIndex(this.deployments, {selected: true});
      const firstDeployment = this.deployments[indexOfFirstSelectedElem];
      this.resourceService.canCreateShakedownTest(firstDeployment.appServerId).subscribe(
        /* happy path */ (r) => this.hasPermissionShakedownTest = r,
        /* error path */ (e) => this.errorMessage = e,
        /* onComplete */  () => $('#deploymentsEdit').modal('show')
      );
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
    this.csvReadyObjects = {};
    this.errorMessage = 'Generating your CSV.<br>Please hold on, depending on the requested data this may take a while';
    this.getFilteredDeploymentsForExport(JSON.stringify(this.filtersForBackend));
  }

  copyURL() {
    const url: string = decodeURIComponent(window.location.href);
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
    this.applyFilters();
  }

  setMaxResultsPerPage(max: number) {
    this.maxResults = max;
    this.offset = 0;
    this.applyFilters();
  }

  setNewOffset(offset: number) {
    this.offset = offset;
    this.applyFilters();
  }

  reloadDeployment(deploymentId: number) {
    let reloadedDeployment: Deployment;
    this.deploymentService.getWithActions(deploymentId).subscribe(
      /* happy path */ (r) => reloadedDeployment = r,
      /* error path */ (e) => this.errorMessage = e,
      /* on complete */ () => this.updateDeploymentsList(reloadedDeployment)
    );
  }

  public getSelectedDeployments(): Deployment[] {
    return this.deployments.filter((deployment) => deployment.selected === true);
  }

  private canFilterBeAdded(): boolean {
    return this.selectedFilterType.name !== 'Latest deployment job for App Server and Env' ||
      _.findIndex(this.filters, {name: this.selectedFilterType.name}) === -1;
  }

  private enhanceDeploymentsForExport() {
    if (this.deploymentsForExport) {
      this.deploymentsForExport.forEach((deployment) => {
        this.getDeploymentDetailForCsvExport(deployment);
      });
    }
  }

  private populateCSVrows(deployment: Deployment) {
    const detail: DeploymentDetail = this.deploymentDetailMap[deployment.id];
    const csvReadyObject: any = {
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
    this.csvReadyObjects[deployment.id] = csvReadyObject;
    if (Object.keys(this.csvReadyObjects).length === this.deploymentsForExport.length) {
      this.csvDocument = this.createCSV();
      const docName: string = 'deployments_' + moment().format('YYYY-MM-DD_HHmm').toString() + '.csv';
      this.pushDownload(docName);
      this.errorMessage = '';
    }
  }

  private createCSV(): string {
    let content: string = this.createCSVTitles();
    this.deploymentsForExport.forEach((entry) => {
      const deployment: any = this.csvReadyObjects[entry.id];
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
            if (deployment[field]) {
              line += '"';
            }
            deployment[field].forEach((appsWithVersion) => {
              line += appsWithVersion['applicationName'] + ' ' + appsWithVersion['version'] + '\n';
            });
            line = line.replace(/\n$/, "\"");
            line += this.csvSeparator;
            break;
          case 'deploymentParameters':
            if (deployment[field]) {
              line += '"';
            }
            deployment[field].forEach((deploymentParameter) => {
              line += deploymentParameter['key'] + ' ' + deploymentParameter['value'] + '\n';
            });
            line = line.replace(/\n$/, "\"");
            line += this.csvSeparator;
            break;
          case 'stateMessage':
            line += deployment[field] !== null ? '"' + deployment[field].replace(/"/g, "").replace(/\n$/, "") + '"' + this.csvSeparator : this.csvSeparator;
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
    const labelsArray: string[]  = [
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
    const blob = new Blob([this.csvDocument], { type: 'text/csv' });
    const url = window.URL.createObjectURL(blob);
    if (navigator.msSaveOrOpenBlob) {
      navigator.msSaveBlob(blob, docName);
    } else {
      const a = document.createElement('a');
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

  private setDeploymentDate(deployment: Deployment, deploymentDate: number) {
    this.deploymentService.setDeploymentDate(deployment.id, deploymentDate).subscribe(
      /* happy path */ (r) => r,
      /* error path */ (e) => this.errorMessage = this.errorMessage ? this.errorMessage + '<br>' + e : e,
      /* on complete */ () => this.reloadDeployment(deployment.id)
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

  private getFilteredDeploymentsForExport(filterString: string) {
    this.deploymentService.getFilteredDeployments(filterString, this.sortCol, this.sortDirection, 0, 0).subscribe(
      /* happy path */ (r) => this.deploymentsForExport = r.deployments,
      /* error path */ (e) => this.errorMessage = e,
      /* onComplete */ () => { this.mapStates();
                               this.enhanceDeploymentsForExport(); }
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
        const i: number = _.findIndex(this.filterTypes, ['name', filter.name]);
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
      this.applyFilters();
    }
  }

  private populateMap() {
    this.comparatorOptions.forEach((option) => {
      this.comparatorOptionsMap[option.name] = option.displayName;
    });
    this.isLoading = false;
  }

  private updateFiltersInURL(destination: string) {
    if (destination) {
      this.location.replaceState('/deployments?filters=' + destination);
    } else {
      this.location.replaceState('/deployments');
    }
  }

}
