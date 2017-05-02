import { Component, OnInit } from '@angular/core';
import { PermissionService } from  './permission.service';
import { AppState } from '../app.service';
import { Restriction } from './restriction';
import * as _ from 'lodash';

@Component({
  selector: 'amw-permission',
  templateUrl: './permission.component.html',
  providers: [ PermissionService ]
})

export class PermissionComponent implements OnInit {

  roleNames: string[] = [];
  permissionNames: string[] = [];
  restrictions: Restriction[] = [];

  selectedRoleName: string = '';

  errorMessage: string = '';
  successMessage: string = '';
  isLoading: boolean = false;

  constructor(private permissionService: PermissionService,
              private appState: AppState) {
  }

  ngOnInit() {

    this.appState.set('navShow', true);
    this.appState.set('navTitle', 'Roles');
    this.appState.set('pageTitle', 'Permissions');

    console.log('hello `Permission` component');

    this.getAllRoleNames();

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

  private getRoleWithRestrictions(roleName: string) {
    this.isLoading = true;
    this.permissionService
      .getRoleWithRestrictions(roleName).subscribe(
      /* happy path */ (r) => this.restrictions = r,
      /* error path */ (e) => this.errorMessage = e,
      /* onComplete */ () => this.isLoading = false);
  }

}
