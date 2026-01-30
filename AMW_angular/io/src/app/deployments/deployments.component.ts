import { Location } from '@angular/common';
import { ChangeDetectionStrategy, Component, OnInit, ViewChild, inject, signal } from '@angular/core';
import { FormsModule, NgModel } from '@angular/forms';
import { ActivatedRoute, Params } from '@angular/router';
import * as _ from 'lodash-es';
import * as datefns from 'date-fns';
import { Subscription, timer } from 'rxjs';
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
import { NotificationComponent } from '../shared/elements/notification/notification.component';
import { LoadingIndicatorComponent } from '../shared/elements/loading-indicator.component';
import { PageComponent } from '../layout/page/page.component';
import { ToastService } from '../shared/elements/toast/toast.service';
import { ButtonComponent } from '../shared/button/button.component';
import { DeploymentFilterComponent } from './deployment-filter/deployment-filter.component';

@Component({
  selector: 'app-deployments',
  templateUrl: './deployments.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    LoadingIndicatorComponent,
    NotificationComponent,
    FormsModule,
    IconComponent,
    DeploymentsListComponent,
    PaginationComponent,
    PageComponent,
    ButtonComponent,
    DeploymentFilterComponent,
  ],
})
export class DeploymentsComponent implements OnInit {
  private activatedRoute = inject(ActivatedRoute);
  private location = inject(Location);
  private deploymentService = inject(DeploymentService);
  private modalService = inject(NgbModal);
  private toastService = inject(ToastService);

  defaultComparator = 'eq';

  // initially by queryParam
  paramFilters: DeploymentFilter[] = [];
  autoload = true;

  // valid for all, loaded once
  filterTypes = signal<DeploymentFilterType[]>([]);
  comparatorOptions: ComparatorFilterOption[] = [];
  comparatorOptionsMap: { [key: string]: string } = {};
  singleComparatorOption: ComparatorFilterOption[] = [{ name: 'eq', displayName: 'is' }];
  hasPermissionToRequestDeployments = false;
  csvSeparator = '';

  // available edit actions
  deploymentDate: number; // for deployment date change

  // to be added
  selectedFilterType: DeploymentFilterType;

  // already set
  filters = signal<DeploymentFilter[]>([]);

  // filtered deployments
  deployments = signal<Deployment[]>([]);

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
  isLoading = signal(true);

  @ViewChild('selectModel', { static: true })
  selectModel: NgModel;

  ngOnInit() {
    this.activatedRoute.queryParams.subscribe((param: Params) => {
      if (param.filters) {
        try {
          this.paramFilters = JSON.parse(param.filters);
        } catch {
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
      this.filters.update((filters) => [...filters, newFilter]);
      this.offset = 0;
      this.selectedFilterType = null;
      this.selectModel.reset(null);
    }
  }

  removeFilter(filter: DeploymentFilter) {
    const i: number = _.findIndex(this.filters(), {
      name: filter.name,
      comp: filter.comp,
      val: filter.val,
    });
    if (i !== -1) {
      this.filters.update((filters) => {
        const newFilters = [...filters];
        newFilters.splice(i, 1);
        return newFilters;
      });
    }
    this.offset = 0;
  }

  clearFilters() {
    this.filters.set([]);
    sessionStorage.setItem('deploymentFilters', null);
    this.updateFiltersInURL(null);
  }

  applyFilters() {
    const filtersToBeRemoved: DeploymentFilter[] = [];
    this.errorMessage = '';
    // Remove empty filters first
    this.filters().forEach((filter) => {
      const filterType = this.getFilterType(filter.name);
      if (!filter.val && filterType !== 'SpecialFilterType') {
        filtersToBeRemoved.push(filter);
      } else if (filterType === 'DateType' && !filter.val) {
        this.errorMessage = 'Invalid date';
      }
    });
    filtersToBeRemoved.forEach((filter) => this.removeFilter(filter));

    if (!this.errorMessage) {
      this.getFilteredDeployments(this.buildBackendFilters());
      const filterString = this.filters().length > 0 ? JSON.stringify(this.filters()) : null;
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
    this.deployments.update((deps) => {
      deps.forEach((deployment) => (deployment.selected = enable));
      return [...deps];
    });
  }

  editableDeployments(): boolean {
    return _.findIndex(this.deployments(), { selected: true }) !== -1;
  }

  showEdit() {
    if (!this.editableDeployments()) {
      return;
    }

    const modalRef = this.modalService.open(DeploymentsEditModalComponent);
    modalRef.componentInstance.deployments = this.getSelectedDeployments();

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
    this.isLoading.set(true);
    this.errorMessage = 'Generating your CSV.<br>Please hold on, depending on the requested data this may take a while';
    this.getFilteredDeploymentsForCsvExport(this.buildBackendFilters());
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
    return this.deployments().filter((deployment) => deployment.selected === true);
  }

  autoRefresh() {
    if (this.refreshInterval > 0 && !this.timerSubscription) {
      this.timerSubscription = timer(this.refreshInterval * 1000).subscribe(() => {
        this.getFilteredDeployments(this.buildBackendFilters());
        this.timerSubscription = null;
      });
    }
  }

  getFilterType(filterName: string): string | undefined {
    return this.filterTypes().find((ft) => ft.name === filterName)?.type;
  }

  private canFilterBeAdded(): boolean {
    return (
      this.selectedFilterType.name !== 'Latest deployment job for App Server and Env' ||
      _.findIndex(this.filters(), { name: this.selectedFilterType.name }) === -1
    );
  }

  private pushDownload(prefix: string) {
    this.isLoading.set(false);
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
    this.deployments.update((deps) => {
      const index = _.findIndex(deps, { id: deployment.id });
      if (index !== -1) {
        const newDeps = [...deps];
        newDeps[index] = deployment;
        return newDeps;
      }
      return deps;
    });
  }

  private comparatorOptionsForType(filterType: string) {
    if (filterType === 'booleanType' || filterType === 'StringType' || filterType === 'ENUM_TYPE') {
      return this.singleComparatorOption;
    } else {
      return this.comparatorOptions;
    }
  }

  comparatorOptionsForFilterType(filterType: string) {
    return this.comparatorOptionsForType(filterType);
  }

  private buildBackendFilters(): string {
    const filters = this.filters().map(
      (filter) =>
        ({
          name: filter.name,
          comp: filter.comp,
          val: this.getFilterType(filter.name) === 'DateType' ? filter.val.toEpoch().toString() : filter.val,
        }) as DeploymentFilter,
    );
    return JSON.stringify(filters);
  }

  private mapStates() {
    if (this.deployments()) {
      this.deployments().forEach((deployment) => {
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
    this.isLoading.set(true);
    this.deploymentService.getAllDeploymentFilterTypes().subscribe({
      next: (r) => this.filterTypes.set(_.sortBy(r, 'name')),
      error: (e) => (this.errorMessage = e),
      complete: () => {
        this.getAllComparatorOptions();
      },
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

  private getFilteredDeployments(filterString: string) {
    this.isLoading.set(true);
    this.deploymentService
      .getFilteredDeployments(filterString, this.sortCol, this.sortDirection, this.offset, this.maxResults)
      .subscribe({
        next: (r) => {
          this.deployments.set(r.deployments);
          this.allResults = r.total;
          this.currentPage = Math.floor(this.offset / this.maxResults) + 1;
          this.lastPage = Math.ceil(this.allResults / this.maxResults);
        },
        error: (e) => {
          this.errorMessage = e;
          this.isLoading.set(false);
        },
        complete: () => {
          this.isLoading.set(false);
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
    if (this.paramFilters && this.paramFilters.length > 0) {
      this.clearFilters();
      const enhancedFilters: DeploymentFilter[] = [];

      this.paramFilters.forEach((filter) => {
        const filterType = this.getFilterType(filter.name);
        if (filterType) {
          filter.comp = !filter.comp ? this.defaultComparator : filter.comp;
          this.parseDateTime(filter, filterType);
          enhancedFilters.push(filter);
        } else {
          this.errorMessage = 'Error parsing filter';
        }
      });

      this.filters.set(enhancedFilters);
      if (this.autoload) {
        this.applyFilters();
      }
    } else if (this.autoload) {
      this.applyFilters();
    }
  }

  // parse string from json back to DateTimeModel
  private parseDateTime(filter: DeploymentFilter, filterType: string) {
    if (filterType === 'DateType') {
      filter.val = DateTimeModel.fromLocalString(filter.val);
    }
  }

  private populateMap() {
    this.comparatorOptions.forEach((option) => {
      this.comparatorOptionsMap[option.name] = option.displayName;
    });
    this.isLoading.set(false);
  }

  private updateFiltersInURL(destination: string) {
    if (destination) {
      this.location.replaceState('/deployments?filters=' + destination);
    } else {
      this.location.replaceState('/deployments');
    }
  }
}
