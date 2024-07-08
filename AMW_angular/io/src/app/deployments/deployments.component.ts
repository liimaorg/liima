import { Location, NgFor, NgIf } from '@angular/common';
import { Component, OnInit, ViewChild } from '@angular/core';
import { FormsModule, NgModel } from '@angular/forms';
import { ActivatedRoute, Params } from '@angular/router';
import * as _ from 'lodash';
import * as datefns from 'date-fns';
import { Subscription, timer } from 'rxjs';
import { ResourceService } from '../resource/resource.service';
import { DeploymentFilter } from '../deployment/deployment-filter';
import { DeploymentFilterType } from '../deployment/deployment-filter-type';
import { ComparatorFilterOption } from '../deployment/comparator-filter-option';
import { Deployment } from '../deployment/deployment';
import { DeploymentService } from '../deployment/deployment.service';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { DeploymentsEditModalComponent } from './deployments-edit-modal.component';
import { DateTimeModel } from '../shared/date-time-picker/date-time.model';
import { PaginationComponent } from '../shared/pagination/pagination.component';
import { DeploymentsListComponent } from './deployments-list.component';
import { IconComponent } from '../shared/icon/icon.component';
import { DateTimePickerComponent } from '../shared/date-time-picker/date-time-picker.component';
import { NotificationComponent } from '../shared/elements/notification/notification.component';
import { LoadingIndicatorComponent } from '../shared/elements/loading-indicator.component';
import { PageComponent } from '../layout/page/page.component';
import { ToastService } from '../shared/elements/toast/toast.service';

declare let $: any;

@Component({
  selector: 'app-deployments',
  templateUrl: './deployments.component.html',
  standalone: true,
  imports: [
    LoadingIndicatorComponent,
    NgIf,
    NotificationComponent,
    FormsModule,
    NgFor,
    DateTimePickerComponent,
    IconComponent,
    DeploymentsListComponent,
    PaginationComponent,
    PageComponent,
  ],
})
export class DeploymentsComponent implements OnInit {
  defaultComparator = 'eq';

  // initially by queryParam
  paramFilters: DeploymentFilter[] = [];
  autoload = true;

  // enhanced filters for deployment service
  filtersForBackend: DeploymentFilter[] = [];
  // value of filters parameter. Used to pass as json object to the logView.xhtml
  filtersForParam: DeploymentFilter[] = [];

  // valid for all, loaded once
  filterTypes: DeploymentFilterType[] = [];
  comparatorOptions: ComparatorFilterOption[] = [];
  comparatorOptionsMap: { [key: string]: string } = {};
  hasPermissionToRequestDeployments = false;
  csvSeparator = '';

  // available edit actions
  hasPermissionShakedownTest = false;
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
  csvDocument: ArrayBuffer;

  // sorting with default values
  sortCol = 'd.deploymentDate';
  sortDirection = 'DESC';

  // pagination with default values
  maxResults = 10;
  offset = 0;
  allResults: number;
  currentPage: number;
  lastPage: number;

  // auto refresh
  refreshIntervals: number[] = [0, 5, 10, 30, 60, 120];
  refreshInterval = 0;
  timerSubscription: Subscription;

  errorMessage = '';
  successMessage = '';
  isLoading = true;

  @ViewChild('selectModel', { static: true })
  selectModel: NgModel;

  constructor(
    private activatedRoute: ActivatedRoute,
    private location: Location,
    private deploymentService: DeploymentService,
    private resourceService: ResourceService,
    private modalService: NgbModal,
    private toastService: ToastService,
  ) {}

  ngOnInit() {
    this.activatedRoute.queryParams.subscribe((param: Params) => {
      if (param.filters) {
        try {
          this.paramFilters = JSON.parse(param.filters);
        } catch (e) {
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
      this.offset = 0;
      this.selectedFilterType = null;
      this.selectModel.reset(null);
    }
  }

  removeFilter(filter: DeploymentFilter) {
    const i: number = _.findIndex(this.filters, {
      name: filter.name,
      comp: filter.comp,
      val: filter.val,
    });
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
        this.filtersForParam.push({
          name: filter.name,
          comp: filter.comp,
          val: filter.val,
        } as DeploymentFilter);
        if (filter.type === 'DateType') {
          if (!filter.val) {
            this.errorMessage = 'Invalid date';
          }
          this.filtersForBackend.push({
            name: filter.name,
            comp: filter.comp,
            val: filter.val.toEpoch().toString(),
          } as DeploymentFilter);
        } else {
          this.filtersForBackend.push({
            name: filter.name,
            comp: filter.comp,
            val: filter.val,
          } as DeploymentFilter);
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
    this.deployments.forEach((deployment) => (deployment.selected = enable));
  }

  editableDeployments(): boolean {
    return _.findIndex(this.deployments, { selected: true }) !== -1;
  }

  showEdit() {
    if (!this.editableDeployments()) {
      return;
    }
    // get shakeDownTestPermission for first element
    const indexOfFirstSelectedElem = _.findIndex(this.deployments, {
      selected: true,
    });
    const firstDeployment = this.deployments[indexOfFirstSelectedElem];
    this.resourceService.canCreateShakedownTest(firstDeployment.appServerId).subscribe({
      next: (r) => (this.hasPermissionShakedownTest = r),
      error: (e) => (this.errorMessage = e),
      complete: () => {
        const modalRef = this.modalService.open(DeploymentsEditModalComponent);
        modalRef.componentInstance.deployments = this.getSelectedDeployments();
        modalRef.componentInstance.hasPermissionShakedownTest = this.hasPermissionShakedownTest;

        modalRef.componentInstance.doConfirmDeployment.subscribe((deployment: Deployment) =>
          this.confirmDeployment(deployment),
        );
        modalRef.componentInstance.doRejectDeployment.subscribe((deployment: Deployment) =>
          this.rejectDeployment(deployment),
        );
        modalRef.componentInstance.doCancelDeployment.subscribe((deployment: Deployment) =>
          this.cancelDeployment(deployment),
        );
        modalRef.componentInstance.doEditDeploymentDate.subscribe((deployment: Deployment) =>
          this.changeDeploymentDate(deployment),
        );
      },
    });
  }

  confirmDeployment(deployment: Deployment) {
    if (deployment) {
      delete deployment.selected;
      deployment.state = this.reMapState(deployment.state);
      this.deploymentService.confirmDeployment(deployment).subscribe({
        next: (r) => r,
        error: (e) => (this.errorMessage = this.errorMessage ? this.errorMessage + '<br>' + e : e),
        complete: () => this.reloadDeployment(deployment.id),
      });
    }
  }

  rejectDeployment(deployment: Deployment) {
    if (deployment) {
      this.deploymentService.rejectDeployment(deployment.id).subscribe({
        next: (r) => r,
        error: (e) => (this.errorMessage = this.errorMessage ? this.errorMessage + '<br>' + e : e),
        complete: () => this.reloadDeployment(deployment.id),
      });
    }
  }

  cancelDeployment(deployment: Deployment) {
    if (deployment) {
      this.deploymentService.cancelDeployment(deployment.id).subscribe({
        next: (r) => r,
        error: (e) => (this.errorMessage = this.errorMessage ? this.errorMessage + '<br>' + e : e),
        complete: () => this.reloadDeployment(deployment.id),
      });
    }
  }

  exportCSV() {
    this.isLoading = true;
    this.errorMessage = 'Generating your CSV.<br>Please hold on, depending on the requested data this may take a while';
    this.getFilteredDeploymentsForCsvExport(JSON.stringify(this.filtersForBackend));
  }

  async copyURL() {
    const url: string = decodeURIComponent(window.location.href);
    await navigator.clipboard.writeText(url);
    this.toastService.success('URL copied to clipboard.');
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
    this.deploymentService.getWithActions(deploymentId).subscribe({
      next: (r) => (reloadedDeployment = r),
      error: (e) => (this.errorMessage = e),
      complete: () => this.updateDeploymentsList(reloadedDeployment),
    });
  }

  getSelectedDeployments(): Deployment[] {
    return this.deployments.filter((deployment) => deployment.selected === true);
  }

  autoRefresh() {
    if (this.refreshInterval > 0 && !this.timerSubscription) {
      this.timerSubscription = timer(this.refreshInterval * 1000).subscribe(() => {
        this.getFilteredDeployments(JSON.stringify(this.filtersForBackend));
        this.timerSubscription = null;
      });
    }
  }

  private canFilterBeAdded(): boolean {
    return (
      this.selectedFilterType.name !== 'Latest deployment job for App Server and Env' ||
      _.findIndex(this.filters, { name: this.selectedFilterType.name }) === -1
    );
  }

  private pushDownload(prefix: string) {
    this.isLoading = false;
    const docName: string = prefix + '_' + datefns.format(new Date(), 'yyyy-MM-dd_HHmm').toString() + '.csv';
    const blob = new Blob([this.csvDocument], { type: 'text/csv' });
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = docName;
    a.setAttribute('style', 'display:none;');
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    window.URL.revokeObjectURL(url);
    this.errorMessage = null;
  }

  private setDeploymentDate(deployment: Deployment, deploymentDate: number) {
    this.deploymentService.setDeploymentDate(deployment.id, deploymentDate).subscribe({
      next: (r) => r,
      error: (e) => (this.errorMessage = this.errorMessage ? this.errorMessage + '<br>' + e : e),
      complete: () => this.reloadDeployment(deployment.id),
    });
  }

  private updateDeploymentsList(deployment: Deployment) {
    this.deployments.splice(_.findIndex(this.deployments, { id: deployment.id }), 1, deployment);
  }

  private comparatorOptionsForType(filterType: string) {
    if (filterType === 'booleanType' || filterType === 'StringType' || filterType === 'ENUM_TYPE') {
      return [{ name: 'eq', displayName: 'is' }];
    } else {
      return this.comparatorOptions;
    }
  }

  private setValueOptionsForFilter(filter: DeploymentFilter) {
    if (!this.filterValueOptions[filter.name]) {
      if (filter.type === 'booleanType') {
        filter.valOptions = this.filterValueOptions[filter.name] = ['true', 'false'];
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

  private reMapState(state: string) {
    switch (state) {
      case 'pre_deploy':
        return 'PRE_DEPLOYMENT';
      case 'ready_for_deploy':
        return 'READY_FOR_DEPLOYMENT';
      default:
        return state;
    }
  }

  private initTypeAndOptions() {
    this.isLoading = true;
    this.deploymentService.getAllDeploymentFilterTypes().subscribe({
      next: (r) => (this.filterTypes = _.sortBy(r, 'name')),
      error: (e) => (this.errorMessage = e),
      complete: () => this.getAllComparatorOptions(),
    });
  }

  private getAllComparatorOptions() {
    this.deploymentService.getAllComparatorFilterOptions().subscribe({
      next: (r) => (this.comparatorOptions = r),
      error: (e) => (this.errorMessage = e),
      complete: () => {
        this.populateMap();
        this.enhanceParamFilter();
      },
    });
  }

  private getAndSetFilterOptionValues(filter: DeploymentFilter) {
    this.deploymentService.getFilterOptionValues(filter.name).subscribe({
      next: (r) => (this.filterValueOptions[filter.name] = r),
      error: (e) => (this.errorMessage = e),
      complete: () => (filter.valOptions = this.filterValueOptions[filter.name]),
    });
  }

  private getFilteredDeployments(filterString: string) {
    this.isLoading = true;
    this.deploymentService
      .getFilteredDeployments(filterString, this.sortCol, this.sortDirection, this.offset, this.maxResults)
      .subscribe({
        next: (r) => {
          this.deployments = r.deployments;
          this.allResults = r.total;
          this.currentPage = Math.floor(this.offset / this.maxResults) + 1;
          this.lastPage = Math.ceil(this.allResults / this.maxResults);
        },
        error: (e) => {
          this.errorMessage = e;
          this.isLoading = false;
        },
        complete: () => {
          this.isLoading = false;
          this.mapStates();
          this.autoRefresh();
        },
      });
  }

  private getFilteredDeploymentsForCsvExport(filterString: string) {
    this.deploymentService
      .getFilteredDeploymentsForCsvExport(filterString, this.sortCol, this.sortDirection)
      .subscribe({
        next: (r) => (this.csvDocument = r),
        error: (e) => (this.errorMessage = e),
        complete: () => this.pushDownload('deployments'),
      });
  }

  private canRequestDeployments() {
    this.deploymentService.canRequestDeployments().subscribe({
      next: (r) => (this.hasPermissionToRequestDeployments = r),
      error: (e) => (this.errorMessage = e),
    });
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
          this.parseDateTime(filter);
          this.setValueOptionsForFilter(filter);
          this.filters.push(filter);
        } else {
          this.errorMessage = 'Error parsing filter';
        }
      });
    }
    if (this.autoload) {
      this.applyFilters();
    }
  }

  // parse string from json back to DateTimeModel
  private parseDateTime(filter: DeploymentFilter) {
    if (filter.type === 'DateType') {
      filter.val = DateTimeModel.fromLocalString(filter.val);
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
