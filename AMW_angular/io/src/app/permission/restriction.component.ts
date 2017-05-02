import { Component, Input, Output, EventEmitter } from '@angular/core';
import { Restriction } from './restriction';
import { Environment } from '../deployment/environment';
import { Resource } from '../resource/resource';
import { ResourceType } from '../resource/resource-type';

@Component({
  selector: 'amw-restriction',
  template: `
    <div>
    
        <div class="form-group" *ngIf="permissionNames.length > 0">
          <label for="selectPermission" class="col-sm-3 control-label">Permission</label>
          <div class="col-sm-9 col-md-5">
            <select id="selectPermission" class="form-control input-sm" [(ngModel)]="restriction.permission">
              <option [ngValue]="permission" *ngFor="let permission of permissionNames">{{permission}}</option>
            </select>
          </div>
        </div>
        
        <div class="form-group">
          <label for="selectAction" class="col-sm-3 control-label">Action</label>
          <div class="col-sm-9 col-md-5">
            <select id="selectAction" class="form-control input-sm" [(ngModel)]="restriction.action">
              <option [ngValue]="action" *ngFor="let action of actions">{{action}}</option>
            </select>
          </div>
        </div>
        
        <div class="form-group" *ngIf="environments.length > 0">
          <label for="selectEnvironment" class="col-sm-3 control-label">Environment</label>
          <div class="col-sm-9 col-md-5">
            <select id="selectEnvironment" class="form-control input-sm" [(ngModel)]="restriction.contextName">
              <option [ngValue]="environment.name" *ngFor="let environment of environments">{{environment.name}}</option>
            </select>
          </div>
        </div>
        
        <div class="form-group" *ngIf="resourceGroups.length > 0">
          <label for="selectResourceGroup" class="col-sm-3 control-label">Resource Group</label>
          <div class="col-sm-9 col-md-5">
            <select id="selectResourceGroup" class="form-control input-sm" [(ngModel)]="restriction.resourceGroupId">
              <option [ngValue]="resource.id" *ngFor="let resource of resourceGroups">{{resource.name}}</option>
            </select>
          </div>
        </div>
        
        <div class="form-group" *ngIf="resourceTypes.length > 0">
          <label for="selectResourceType" class="col-sm-3 control-label">Resource Type</label>
          <div class="col-sm-9 col-md-5">
            <select id="selectResourceType" class="form-control input-sm" [(ngModel)]="restriction.resourceTypeName">
              <option [ngValue]="resourceType.name" *ngFor="let resourceType of resourceTypes">{{resourceType.name}}</option>
            </select>
          </div>
        </div>

    ResourceTypePermission: {{restriction.resourceTypePermission}}
    <br>
    <button type="button" class="btn btn-danger btn-sm" (click)="removeRestriction(restriction.id)">
        <span class="glyphicon glyphicon-remove-circle" aria-hidden="true"></span>
    </button>
    </div>
`
})

export class RestrictionComponent {

  actions: string[] = [ 'ALL', 'CREATE', 'READ', 'UPDATE', 'DELETE' ];

  @Input() restriction: Restriction;
  @Input() permissionNames: string[] = [];
  @Input() environments: Environment[] = [];
  @Input() resourceGroups: Resource[] = [];
  @Input() resourceTypes: ResourceType[] = [];
  @Output() deleteRestriction: EventEmitter<number> = new EventEmitter<number>();

  removeRestriction(id: number) {
    this.deleteRestriction.emit(id);
  }
}
