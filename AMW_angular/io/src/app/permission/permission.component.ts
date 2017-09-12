import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { PermissionService } from  './permission.service';
import { Environment } from '../deployment/environment';
import { EnvironmentService } from '../deployment/environment.service';
import { Resource } from '../resource/resource';
import { ResourceType } from '../resource/resource-type';
import { ResourceService } from '../resource/resource.service';
import { AppState } from '../app.service';
import { Restriction } from './restriction';
import { Permission } from './permission';
import * as _ from 'lodash';

@Component({
  selector: 'amw-permission',
  templateUrl: './permission.component.html'
})

export class PermissionComponent implements OnInit, OnDestroy {

  // loaded only once
  roleNames: string[] = [];
  userNames: string[] = [];
  permissions: Permission[] = [];
  environments: Environment[] = [ { id: null, name: null, parent: 'All', selected: false } ];
  groupedEnvironments: { [key: string]: Environment[] } = { 'All': [], 'Global': [] };
  resourceGroups: Resource[] = [];
  resourceTypes: ResourceType[] = [ { id: null, name: null } ];

  // role | user
  restrictionType: string = 'role';
  restrictions: Restriction[] = [];
  selectedRoleName: string = null;
  selectedUserName: string = null;
  // edit or add restriction
  restriction: Restriction = null;
  // backup for cancel
  backupRestriction: Restriction = null;

  errorMessage: string = '';
  successMessage: string = '';
  isLoading: boolean = false;

  constructor(private permissionService: PermissionService,
              private environmentService: EnvironmentService,
              private resourceService: ResourceService,
              private activatedRoute: ActivatedRoute,
              public appState: AppState) {
  }

  ngOnInit() {
    console.log('hello `Permission` component');

    this.appState.set('navShow', true);
    this.appState.set('navItems', [ { title: 'Roles', target: '/permission/role' }, { title: 'Users', target: '/permission/user' } ]);
    this.appState.set('pageTitle', 'Permissions');

    this.activatedRoute.params.subscribe(
      (param: any) => {
        this.onChangeType(param['restrictionType']);
    });

    this.getAllPermissions();
    this.getAllEnvironments();
    this.getAllResourceGroups();
    this.getAllResourceTypes();
  }

  ngOnDestroy() {
    this.appState.set('navItems', null);
  }

  onChangeRole() {
    console.log('onChangeRole');
    if (this.isExistingRole(this.selectedRoleName)) {
      this.getRoleWithRestrictions(this.selectedRoleName);
    } else {
      this.restrictions = [];
    }
    this.restriction = null;
  }

  onChangeUser() {
    console.log('onChangeUser');
    if (this.isExistingUser(this.selectedUserName)) {
      this.getUserWithRestrictions(this.selectedUserName);
    } else {
      this.restrictions = [];
    }
    this.restriction = null;
  }

  removeRestriction(id: number) {
    if (id) {
      this.permissionService.removeRestriction(id).subscribe(
        /* happy path */ (r) => '',
        /* error path */ (e) => this.errorMessage = e,
        /* onComplete */ () => _.remove(this.restrictions, {id: id}));
    } else {
      this.restriction = null;
    }
  }

  cancel() {
    // reset restriction list, rollback to the last persisted state
    this.resetPermissionList();
    this.restriction = null;
    this.backupRestriction = null;
  }

  modifyRestriction(restriction: Restriction) {
    // reset restriction list, discard unsaved changes
    this.resetPermissionList();
    this.backupRestriction = {...restriction};
    this.restriction = restriction;
  }

  persistRestriction() {
    if (this.restriction.id != null) {
      this.permissionService.updateRestriction(this.restriction).subscribe(
        /* happy path */ (r) => '',
        /* error path */ (e) => this.errorMessage = e,
        /* onComplete */ () => {
          this.updatePermissions(this.restriction);
          this.updateNamesLists();
          this.restriction = null;
          this.backupRestriction = null; });
    } else {
      this.permissionService.createRestriction(this.restriction).subscribe(
        /* happy path */ (r) => this.restriction = r,
        /* error path */ (e) => this.errorMessage = e,
        /* onComplete */ () => {
          this.updatePermissions(this.restriction);
          this.updateNamesLists();
          this.restriction = null; });
    }
  }

  addRestriction() {
    this.restriction = { id: null, roleName: this.selectedRoleName, userName: this.selectedUserName, permission: <Permission> {},
      resourceGroupId: null, resourceTypeName: null, resourceTypePermission: null, contextName: null, action: null };
  }

  private updateNamesLists() {
    if (this.restriction) {
      if (this.restriction.roleName && !this.isExistingRole(this.restriction.roleName)) {
        this.roleNames.push(this.restriction.roleName.toLowerCase());
      } else if (this.restriction.userName && !this.isExistingUser(this.restriction.userName)) {
        this.userNames.push(this.restriction.userName.toLowerCase());
      }
    }
  }

  private isExistingRole(roleName: string) {
    return roleName !== null && this.roleNames.indexOf(roleName.toLowerCase()) > -1;
  }

  private isExistingUser(userName: string) {
    return userName !== null && this.userNames.indexOf(userName.toLowerCase()) > -1;
  }

  private onChangeType(type: string) {
    console.log('onChangeType');
    this.errorMessage = '';
    this.successMessage = '';
    this.restrictions = [];
    this.restrictionType = (type === 'role' || type === 'user') ? type : 'role';
    if (this.restrictionType === 'user') {
      this.appState.set('navTitle', 'Users');
      this.selectedRoleName = null;
      if (this.userNames.length < 1) {
        this.getAllUserNames();
      }
    } else {
      this.appState.set('navTitle', 'Roles');
      this.selectedUserName = null;
      if (this.roleNames.length < 1) {
        this.getAllRoleNames();
      }
    }
  }

  private getAllRoleNames() {
    this.isLoading = true;
    this.permissionService
      .getAllRoleNames().subscribe(
      /* happy path */ (r) => this.roleNames = r,
      /* error path */ (e) => this.errorMessage = e,
      /* onComplete */ () => this.isLoading = false);
  }

  private getAllUserNames() {
    this.isLoading = true;
    this.permissionService
      .getAllUserRestrictionNames().subscribe(
      /* happy path */ (r) => this.userNames = r,
      /* error path */ (e) => this.errorMessage = e,
      /* onComplete */ () => this.isLoading = false);
  }

  private getAllPermissions() {
    this.isLoading = true;
    this.permissionService
      .getAllPermissionEnumValues().subscribe(
      /* happy path */ (r) => this.permissions = _.sortBy(r, function(s: Permission) { return s.name.replace(/[_]/, ''); }),
      /* error path */ (e) => this.errorMessage = e,
      /* onComplete */ () => this.isLoading = false);
  }

  private getAllEnvironments() {
    this.isLoading = true;
    this.environmentService
      .getAllIncludingGroups().subscribe(
      /* happy path */ (r) => this.environments = this.environments.concat(r),
      /* error path */ (e) => this.errorMessage = e,
      /* onComplete */ () => this.extractEnvironmentGroups());
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
      /* happy path */ (r) => this.resourceTypes = this.resourceTypes.concat(r),
      /* error path */ (e) => this.errorMessage = e,
      /* onComplete */ () => this.isLoading = false);
  }

  private getRoleWithRestrictions(roleName: string) {
    this.isLoading = true;
    this.permissionService
      .getRoleWithRestrictions(roleName).subscribe(
      /* happy path */ (r) => this.reorderRestrictions(r),
      /* error path */ (e) => this.errorMessage = e,
      /* onComplete */ () => this.isLoading = false);
  }

  private getUserWithRestrictions(userName: string) {
    this.isLoading = true;
    this.permissionService
      .getUserWithRestrictions(userName).subscribe(
      /* happy path */ (r) => this.reorderRestrictions(r),
      /* error path */ (e) => this.errorMessage = e,
      /* onComplete */ () => this.isLoading = false);
  }

  private resetPermissionList() {
    if (this.backupRestriction) {
      this.updatePermissions(this.backupRestriction);
    }
  }

  private updatePermissions(restriction: Restriction) {
    let i = _.findIndex(this.restrictions, _.pick(restriction, 'id'));
    if (i !== -1) {
      this.restrictions.splice(i, 1, restriction);
    } else {
      this.restrictions.push(restriction);
    }
    this.reorderRestrictions(this.restrictions);
  }

  private reorderRestrictions(restrictions: Restriction[]) {
    this.restrictions = _.sortBy(restrictions, [function(s: Restriction) {
      return s.permission.name.replace(/[_]/, ''); }, 'action']);
  }

  private extractEnvironmentGroups() {
    this.environments.forEach((environment) => {
      environment.selected = false;
      if (!this.groupedEnvironments[environment['parent']]) {
        this.groupedEnvironments[environment['parent']] = [];
      }
      this.groupedEnvironments[environment['parent']].push(environment);
    });
    this.isLoading = false;
  }

}
