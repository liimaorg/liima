import { Component, OnInit, NgZone } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { AppState } from '../app.service';
import { ComparatorFilterOption } from './comparator-filter-option';
import { DeploymentFilter } from './deployment-filter';
import { DeploymentFilterType } from './deployment-filter-type';
import { DeploymentService } from './deployment.service';
import { Datetimepicker } from 'eonasdan-bootstrap-datetimepicker';
import * as _ from 'lodash';

declare var $: any;

@Component({
  selector: 'amw-deployments',
  templateUrl: './deployments.component.html'
})

export class DeploymentsComponent implements OnInit {

  // initially by queryParam
  paramFilters: DeploymentFilter[] = [];

  // valid for all, loaded once
  filterTypes: DeploymentFilterType[] = [];
  comparatorOptions: ComparatorFilterOption[] = [];
  comparatorOptionsMap: { [key: string]: string } = {};

  // available filterValues (if any)
  filterValueOptions: { [key: string]: string[] } = {};

  // to be added
  selectedFilterType: DeploymentFilterType;

  defaultComparator: string = 'eq';

  // already set
  filters: DeploymentFilter[] = [];

  errorMessage: string = '';
  successMessage: string = '';
  isLoading: boolean = false;

  constructor(private activatedRoute: ActivatedRoute,
              private ngZone: NgZone,
              private deploymentService: DeploymentService,
              public appState: AppState) {
  }

  ngOnInit() {

    this.appState.set('navShow', false);
    this.appState.set('navTitle', 'Deployments');
    this.appState.set('pageTitle', 'Deployments');

    console.log('hello `Deployments` component');

    this.activatedRoute.queryParams.subscribe(
      (param: any) => {
        if (param['filter']) {
          try {
            this.paramFilters = JSON.parse(param['filter']);
          } catch (e) {
            console.log(e);
            this.errorMessage = 'Error parsing filter';
          }
        }
    });

    this.initTypeAndOptions();

  }

  addFilter() {
    if (this.selectedFilterType) {
      console.log('type: ' + this.selectedFilterType.type);

      let newFilter: DeploymentFilter = <DeploymentFilter> {};

      if (!this.filterValueOptions[this.selectedFilterType.name]) {
        if (this.selectedFilterType.type === 'booleanType') {
          this.filterValueOptions[this.selectedFilterType.name] = [ 'true', 'false' ];
        } else {
          this.getFilterOptionValues(this.selectedFilterType.name);
        }
      }

      newFilter.name = this.selectedFilterType.name;
      newFilter.comp = this.defaultComparator;
      newFilter.val = this.selectedFilterType.type === 'booleanType' ? 'true' : '';
      newFilter.type = this.selectedFilterType.type;
      newFilter.compOptions = this.comparatorOptionsForType(this.selectedFilterType.type);
      this.filters.unshift(newFilter);
      this.enableDatepicker(this.selectedFilterType.type);
    }
  }

  removeFilter(filter: DeploymentFilter) {
    _.remove(this.filters, {name: filter.name});
  }

  private enableDatepicker(filterType: string) {
    if (filterType === 'DateType') {
      this.ngZone.onMicrotaskEmpty.first().subscribe(() => {
        $('.datepicker').datetimepicker({format: 'DD.MM.YYYY HH:mm'});
      });
    }
  }

  private comparatorOptionsForType(filterType: string) {
    if (filterType === 'booleanType' || filterType === 'StringType' || filterType === 'ENUM_TYPE') {
      return [{name: 'eq', displayName: 'is'}];
    } else {
      return this.comparatorOptions;
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
                               this.enhanceParamFilter();
      });
  }

  private getFilterOptionValues(filterName: string) {
    this.isLoading = true;
    this.deploymentService.getFilterOptionValues(filterName).subscribe(
      /* happy path */ (r) => this.filterValueOptions[filterName] = r,
      /* error path */ (e) => this.errorMessage = e,
      /* onComplete */ () => this.isLoading = false);
  }

  private enhanceParamFilter() {
    if (this.paramFilters) {
      this.paramFilters.forEach((filter) => {
        let i: number = _.findIndex(this.filterTypes, ['name', filter.name]);
        if (i >= 0) {
          filter.compOptions = this.comparatorOptionsForType(this.filterTypes[i].type);
          filter.comp = !filter.comp ? this.defaultComparator : filter.comp;
          filter.type = this.filterTypes[i].type;
          this.filterValueOptions[filter.name] = [];
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

}
