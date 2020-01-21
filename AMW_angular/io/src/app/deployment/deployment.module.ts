import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { DeploymentComponent } from './deployment.component';
import { DeploymentsComponent } from './deployments.component';
import { DeploymentsListComponent } from './deployments-list.component';
import { DeploymentsEditModalComponent } from './deployments-edit-modal.component';
import { DeploymentService } from './deployment.service';
import { EnvironmentService } from './environment.service';
import { DeploymentRoutingModule } from './deployment-routing.module';
import { PaginationComponent } from './pagination.component';
import {NgbModule} from "@ng-bootstrap/ng-bootstrap";

@NgModule({
  imports: [CommonModule, FormsModule, DeploymentRoutingModule, NgbModule],
  declarations: [DeploymentComponent, DeploymentsComponent, DeploymentsListComponent, DeploymentsEditModalComponent, PaginationComponent],
  providers: [DeploymentService, EnvironmentService]
})
export class DeploymentModule {
}
