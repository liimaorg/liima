import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { PermissionService } from './permission.service';
import { Restriction } from 'src/app/auth/restriction';
import { RestrictionsCreation } from './restrictions-creation';
import { Permission } from 'src/app/auth/permission';
import * as _ from 'lodash';
import { Environment } from 'src/app/deployment/environment';
import { Resource } from 'src/app/resource/resource';
import { ResourceType } from '../../resource/resource-type';
import { EnvironmentService } from '../../deployment/environment.service';
import { ResourceService } from 'src/app/resource/resource.service';
import { Location } from '@angular/common';
import { RestrictionListComponent } from './restriction-list.component';
import { RestrictionAddComponent } from './restriction-add.component';
import { RestrictionEditComponent } from './restriction-edit.component';
import { IconComponent } from '../../shared/icon/icon.component';
import { NgSelectModule } from '@ng-select/ng-select';
import { FormsModule } from '@angular/forms';
import {
  NgbNav,
  NgbNavItem,
  NgbNavLinkButton,
  NgbNavLinkBase,
  NgbNavContent,
  NgbNavOutlet,
} from '@ng-bootstrap/ng-bootstrap';
import { LoadingIndicatorComponent } from '../../shared/elements/loading-indicator.component';
import { ButtonComponent } from '../../shared/button/button.component';
import { ResourceTypesService } from '../../resource/resource-types.service';

@Component({
  selector: 'app-permission',
  templateUrl: './permission.component.html',
  imports: [
    LoadingIndicatorComponent,
    NgbNav,
    NgbNavItem,
    NgbNavLinkButton,
    NgbNavLinkBase,
    NgbNavContent,
    FormsModule,
    NgSelectModule,
    NgbNavOutlet,
    IconComponent,
    RestrictionEditComponent,
    RestrictionAddComponent,
    RestrictionListComponent,
    ButtonComponent,
  ],
})
export class PermissionComponent implements OnInit {
  // loaded only once
  roleNames: string[] = [];
  userNames: string[] = [];
  permissions: Permission[] = [];
  environments: Environment[] = [{ id: null, name: null, parentName: 'All', selected: false } as Environment];
  groupedEnvironments: { [key: string]: Environment[] } = {
    All: [],
    Global: [],
  };
  resourceGroups: Resource[] = [];
  resourceTypes: ResourceType[] = [
    {
      id: null,
      name: null,
      hasChildren: false,
      hasParent: false,
      children: [],
      isApplication: false,
      isDefaultResourceType: false,
    },
  ];

  restrictionType: string = 'role';
  delegationMode: boolean = false;
  assignedRestrictions: Restriction[] = [];
  selectedRoleName: string = null;
  selectedUserNames: string[] = [];
  actingUserName: string = null;
  assignableRestrictions: Restriction[] = [];
  assignablePermissions: Permission[] = [];

  // edit restriction
  restriction: Restriction = null;
  // backup for cancel
  backupRestriction: Restriction = null;
  // create new restrictions if true
  create: boolean = false;

  errorMessage: string = null;
  successMessage: string = null;
  isLoading: boolean = false;

  constructor(
    private permissionService: PermissionService,
    private environmentService: EnvironmentService,
    private resourceService: ResourceService,
    private resourceTypesService: ResourceTypesService,
    private activatedRoute: ActivatedRoute,
    private location: Location,
  ) {
    this.activatedRoute.params.subscribe((param: any) => {
      if (param['actingUser']) {
        this.delegationMode = true;
        this.restrictionType = 'user';
        this.onChangeActingUser(param['actingUser']);
      } else {
        if (param['restrictionType']) {
          this.restrictionType = param['restrictionType'];
        }
        this.delegationMode = false;
        this.getAllPermissions();
        this.onChangeType(this.restrictionType);
      }
    });
  }

  ngOnInit() {
    this.getAllEnvironments();
    this.getAllResourceGroups();
    this.getAllResourceTypes();
  }

  onChangeRole() {
    this.selectedRoleName = this.selectedRoleName.trim();
    if (this.isExistingRole(this.selectedRoleName)) {
      this.getRoleWithRestrictions(this.selectedRoleName);
    } else {
      this.assignedRestrictions = [];
    }
    this.restriction = null;
  }

  onChangeUser(users: any) {
    this.convertToSelectedUserNames(users);
    if (this.selectedUserNames.length === 1 && this.isExistingUser(this.selectedUserNames[0])) {
      this.getUserWithRestrictions(this.selectedUserNames[0]);
    } else {
      this.assignedRestrictions = [];
    }
    this.restriction = null;
  }

  removeRestriction(id: number) {
    this.clearMessages();
    if (id) {
      this.permissionService.removeRestriction(id).subscribe({
        next: () => '',
        error: (e) => (this.errorMessage = e),
        complete: () => {
          this.assignedRestrictions = this.assignedRestrictions.filter((restriction) => restriction.id !== id);
        },
      });
    } else {
      this.restriction = null;
    }
  }

  cancel() {
    // reset restriction list, rollback to the last persisted state
    this.clearMessages();
    this.resetPermissionList();
    this.restriction = null;
    this.backupRestriction = null;
    this.create = false;
  }

  modifyRestriction(restrictionId: number) {
    // reset restriction list, discard unsaved changes
    this.clearMessages();
    this.resetPermissionList();
    const restriction = this.assignedRestrictions.find((r) => r.id === restrictionId);
    this.backupRestriction = { ...restriction };
    this.restriction = restriction;
  }

  persistRestriction() {
    this.clearMessages();
    this.isLoading = true;
    if (this.restriction.id != null) {
      this.permissionService.updateRestriction(this.restriction).subscribe({
        next: () => '',
        error: (e) => (this.errorMessage = e),
        complete: () => {
          this.updatePermissions(this.restriction);
          this.updateNamesLists();
          this.restriction = null;
          this.backupRestriction = null;
          this.isLoading = false;
          this.successMessage = 'Restriction updated successfully';
        },
      });
    } else {
      this.permissionService.createRestriction(this.restriction, this.delegationMode).subscribe({
        next: (r) => (this.restriction = r),
        error: (e) => (this.errorMessage = e),
        complete: () => {
          this.updatePermissions(this.restriction);
          this.updateNamesLists();
          this.restriction = null;
          this.isLoading = false;
          this.successMessage = 'Restriction created successfully';
        },
      });
    }
  }

  createRestrictions(restrictionsCreation: RestrictionsCreation) {
    this.clearMessages();
    this.isLoading = true;
    this.permissionService.createRestrictions(restrictionsCreation, this.delegationMode).subscribe({
      next: () => '',
      error: (e) => (this.errorMessage = e),
      complete: () => {
        this.create = false;
        this.updateExistingNamesLists(restrictionsCreation);
        this.reloadAssignedRestrictions(restrictionsCreation);
        this.isLoading = false;
        this.successMessage = 'Restriction(s) created successfully';
      },
    });
  }

  addRestriction() {
    this.clearMessages();
    if (this.selectedRoleName || this.selectedUserNames.length > 0) {
      this.create = true;
    }
  }

  getPermissions(): Permission[] {
    return this.delegationMode ? this.assignablePermissions : this.permissions;
  }

  private convertToSelectedUserNames(users: any) {
    this.selectedUserNames = [];
    users.forEach((user) => {
      if (user.label) {
        this.selectedUserNames.push(user.label.trim());
      } else {
        this.selectedUserNames.push(user.trim());
      }
    });
  }

  private reloadAssignedRestrictions(restrictionsCreation: RestrictionsCreation) {
    if (restrictionsCreation.roleName) {
      this.getRoleWithRestrictions(restrictionsCreation.roleName);
    } else if (restrictionsCreation.userNames.length === 1) {
      this.getUserWithRestrictions(restrictionsCreation.userNames[0]);
    }
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

  private updateExistingNamesLists(restrictionsCreation: RestrictionsCreation) {
    if (restrictionsCreation.roleName && !this.isExistingRole(restrictionsCreation.roleName)) {
      this.roleNames.push(restrictionsCreation.roleName.toLowerCase());
    } else if (restrictionsCreation.userNames.length > 0) {
      restrictionsCreation.userNames.forEach((userName) => {
        if (!this.isExistingUser(userName)) {
          this.userNames.push(userName.toLowerCase());
        }
      });
    }
  }

  private isExistingRole(roleName: string) {
    return roleName !== null && this.roleNames.indexOf(roleName.toLowerCase()) > -1;
  }

  private isExistingUser(userName: string) {
    return userName !== null && this.userNames.indexOf(userName.toLowerCase()) > -1;
  }

  private onChangeType(type: string) {
    this.clearMessages();
    this.assignedRestrictions = [];
    this.restrictionType = type === 'role' || type === 'user' ? type : 'role';
    if (this.restrictionType === 'user') {
      this.selectedRoleName = null;
      if (this.userNames.length < 1) {
        this.getAllUserNames();
      }
    } else {
      this.selectedUserNames = [];
      if (this.roleNames.length < 1) {
        this.getAllRoleNames();
      }
    }
  }

  private onChangeActingUser(userName: string) {
    this.clearMessages();
    this.assignedRestrictions = [];
    this.actingUserName = userName;
    this.selectedUserNames = [];
    this.selectedRoleName = null;
    this.getAllAssignableUserNames();
    this.getAllAssignableRestrictions();
  }

  private getAllRoleNames() {
    this.isLoading = true;
    this.permissionService.getAllRoleNames().subscribe({
      next: (r) => (this.roleNames = r),
      error: (e) => (this.errorMessage = e),
      complete: () => (this.isLoading = false),
    });
  }

  private getAllUserNames() {
    this.isLoading = true;
    this.permissionService.getAllUserRestrictionNames().subscribe({
      next: (r) => (this.userNames = r),
      error: (e) => (this.errorMessage = e),
      complete: () => (this.isLoading = false),
    });
  }

  private getAllPermissions() {
    this.isLoading = true;
    this.permissionService.getAllPermissionEnumValues().subscribe({
      next: (r) =>
        (this.permissions = _.sortBy(r, function (s: Permission) {
          return s.name.replace(/[_]/, '');
        })),
      error: (e) => (this.errorMessage = e),
      complete: () => {
        this.markGlobalPermissions(this.permissions);
        this.isLoading = false;
      },
    });
  }

  private markGlobalPermissions(permissions: Permission[]) {
    permissions.forEach((permission) => {
      permission.longName = permission.old ? permission.name + ' (GLOBAL)' : permission.name;
    });
  }

  private getAllEnvironments() {
    this.isLoading = true;
    this.environmentService.getAllIncludingGroups().subscribe({
      next: (r) => (this.environments = this.environments.concat(r)),
      error: (e) => (this.errorMessage = e),
      complete: () => this.extractEnvironmentGroups(),
    });
  }

  private getAllAssignableUserNames() {
    this.isLoading = true;
    this.permissionService.getAllUserRestrictionNames().subscribe({
      next: (r) => (this.userNames = _.pull(r, this.actingUserName)),
      error: (e) => (this.errorMessage = e),
      complete: () => (this.isLoading = false),
    });
  }

  private getAllAssignableRestrictions() {
    this.isLoading = true;
    this.permissionService.getOwnUserAndRoleRestrictions().subscribe({
      next: (r) => (this.assignableRestrictions = r),
      error: (e) => (this.errorMessage = e),
      complete: () => {
        this.extractAllAssignablePermissions();
        this.markGlobalPermissions(this.assignablePermissions);
        this.isLoading = false;
      },
    });
  }

  private extractAllAssignablePermissions() {
    this.assignablePermissions = [];
    this.assignableRestrictions.forEach((restriction) => {
      if (!_.some(this.assignablePermissions, restriction.permission)) {
        this.assignablePermissions.push(restriction.permission);
      }
    });
    this.assignablePermissions = _.sortBy(this.assignablePermissions, function (s: Permission) {
      return s.name.replace(/[_]/, '');
    });
  }

  private getAllResourceGroups() {
    this.isLoading = true;
    this.resourceService.getAllResourceGroups().subscribe({
      next: (r) => (this.resourceGroups = r),
      error: (e) => (this.errorMessage = e),
      complete: () => (this.isLoading = false),
    });
  }

  private getAllResourceTypes() {
    this.isLoading = true;
    this.resourceTypesService.getAllResourceTypes().subscribe({
      next: (r) => (this.resourceTypes = this.resourceTypes.concat(r)),
      error: (e) => (this.errorMessage = e),
      complete: () => (this.isLoading = false),
    });
  }

  private getRoleWithRestrictions(roleName: string) {
    this.isLoading = true;
    this.permissionService.getRoleWithRestrictions(roleName).subscribe({
      next: (r) => this.reorderRestrictions(r),
      error: (e) => (this.errorMessage = e),
      complete: () => (this.isLoading = false),
    });
  }

  private getUserWithRestrictions(userName: string) {
    this.isLoading = true;
    this.permissionService.getUserWithRestrictions(userName).subscribe({
      next: (r) => this.reorderRestrictions(r),
      error: (e) => (this.errorMessage = e),
      complete: () => (this.isLoading = false),
    });
  }

  private resetPermissionList() {
    if (this.backupRestriction) {
      this.updatePermissions(this.backupRestriction);
    }
  }

  private updatePermissions(restriction: Restriction) {
    const i: number = _.findIndex(this.assignedRestrictions, _.pick(restriction, 'id'));
    if (i !== -1) {
      this.assignedRestrictions.splice(i, 1, restriction);
    } else {
      this.assignedRestrictions.push(restriction);
    }
    this.reorderRestrictions(this.assignedRestrictions);
  }

  private reorderRestrictions(restrictions: Restriction[]) {
    this.assignedRestrictions = _.sortBy(restrictions, [
      function (s: Restriction) {
        return s.permission.name.replace(/[_]/, '');
      },
      'action',
    ]);
  }

  private extractEnvironmentGroups() {
    this.environments.forEach((environment) => {
      environment.selected = false;
      if (!this.groupedEnvironments[environment['parentName']]) {
        this.groupedEnvironments[environment['parentName']] = [];
      }
      this.groupedEnvironments[environment['parentName']].push(environment);
    });
    this.isLoading = false;
  }

  private clearMessages() {
    this.errorMessage = null;
    this.successMessage = null;
  }

  changeType(restrictionType: string) {
    this.restrictionType = restrictionType;
    this.onChangeType(this.restrictionType);
    this.location.replaceState(`/settings/permission/${restrictionType}`);
  }
}
