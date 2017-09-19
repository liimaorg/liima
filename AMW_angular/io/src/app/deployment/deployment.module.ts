import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { DataTableModule } from 'angular2-datatable';
import { DeploymentComponent } from './deployment.component';
import { DeploymentsComponent } from './deployments.component';
import { DeploymentsListComponent } from './deployments-list.component';
import { DeploymentService } from './deployment.service';
import { EnvironmentService } from './environment.service';
import { DeploymentRoutingModule } from './deployment-routing.module';

@NgModule({
  imports: [CommonModule, FormsModule, DataTableModule, DeploymentRoutingModule],
  declarations: [DeploymentComponent, DeploymentsComponent, DeploymentsListComponent],
  providers: [DeploymentService, EnvironmentService]
})
export class DeploymentModule {
}
