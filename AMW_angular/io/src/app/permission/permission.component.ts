import { Component, OnInit } from '@angular/core';
import { PermissionService } from  './permission.service';
import { Environment } from '../deployment/environment';
import { EnvironmentService } from '../deployment/environment.service';
import { Resource } from '../resource/resource';
import { ResourceType } from '../resource/resource-type';
import { ResourceService } from '../resource/resource.service';
import { AppState } from '../app.service';
import { Restriction } from './restriction';
import * as _ from 'lodash';

@Component({
  selector: 'amw-permission',
  templateUrl: './permission.component.html',
  providers: [ PermissionService ]
})

export class PermissionComponent implements OnInit {

  // loaded only once
  roleNames: string[] = [];
  permissionNames: string[] = [];
  anyEnvironment: Environment = { id: 0, name: '', parent: '', selected: false };
  environments: Environment[] = [ this.anyEnvironment ];
  resourceGroups: Resource[] = [];
  resourceTypes: ResourceType[] = [];

  restrictions: Restriction[] = [];

  selectedRoleName: string = '';

  errorMessage: string = '';
  successMessage: string = '';
  isLoading: boolean = false;

  constructor(private permissionService: PermissionService,
              private environmentService: EnvironmentService,
              private resourceService: ResourceService,
              private appState: AppState) {
  }

  ngOnInit() {

    this.appState.set('navShow', true);
    this.appState.set('navTitle', 'Roles');
    this.appState.set('pageTitle', 'Permissions');

    console.log('hello `Permission` component');

    this.getAllRoleNames();
    this.getAllPermissionNames();
    this.getAllEnvironments();
    this.getAllResourceGroups();
    this.getAllResourceTypes();

  }

  onChangeRole() {
    this.getRoleWithRestrictions(this.selectedRoleName);
  }

  removeRestriction(id: number) {
    this.permissionService.removeRestriction(id).subscribe(
      /* happy path */ (r) => this.successMessage = 'Successfuly removed '+id,
      /* error path */ (e) => this.errorMessage = e,
      /* onComplete */ () => this.updateAssignedPermissions(id));
  }

  private updateAssignedPermissions(id: number) {
    _.remove(this.restrictions, { id: id });
  }

  private getAllRoleNames() {
    this.isLoading = true;
    this.permissionService
      .getAllRoleNames().subscribe(
      /* happy path */ (r) => this.roleNames = r,
      /* error path */ (e) => this.errorMessage = e,
      /* onComplete */ () => this.isLoading = false);
  }

  private getAllPermissionNames() {
    this.isLoading = true;
    this.permissionService
      .getAllPermissionNames().subscribe(
      /* happy path */ (r) => this.permissionNames = r,
      /* error path */ (e) => this.errorMessage = e,
      /* onComplete */ () => this.isLoading = false);
  }

  private getAllEnvironments() {
    this.isLoading = true;
    this.environmentService
      .getAll().subscribe(
      /* happy path */ (r) => this.environments = this.environments.concat(r),
      /* error path */ (e) => this.errorMessage = e,
      /* onComplete */ () => this.isLoading = false);
  }

  private getAllResourceGroups() {
    this.isLoading = true;
    this.resourceService
      .getAllResourceGroups().subscribe(
      /* happy path */ (r) => this.resourceGroups = r,
      /* error path */ (e) => this.errorMessage = e,
      /* onComplete */ () => this.isLoading = false);
  }

  private getAllResourceTypes() {
    this.isLoading = true;
    this.resourceService
      .getAllResourceTypes().subscribe(
      /* happy path */ (r) => this.resourceTypes = r,
      /* error path */ (e) => this.errorMessage = e,
      /* onComplete */ () => this.isLoading = false);
  }

  private getRoleWithRestrictions(roleName: string) {
    this.isLoading = true;
    this.permissionService
      .getRoleWithRestrictions(roleName).subscribe(
      /* happy path */ (r) => this.restrictions = r,
      /* error path */ (e) => this.errorMessage = e,
      /* onComplete */ () => this.isLoading = false);
  }

}
