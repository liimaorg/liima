import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DeploymentsComponent } from './deployments.component';
import { DeploymentsListComponent } from './deployments-list.component';
import { FormsModule } from '@angular/forms';
import { SharedModule } from '../shared/shared.module';
import { DeploymentsEditModalComponent } from './deployments-edit-modal.component';
import { DeploymentService } from '../deployment/deployment.service';
import { EnvironmentService } from '../deployment/environment.service';
import { DeploymentsRoutingModule } from './deployments-routing.module';

@NgModule({
  imports: [CommonModule, FormsModule, DeploymentsRoutingModule, SharedModule],
  declarations: [
    DeploymentsComponent,
    DeploymentsListComponent,
    DeploymentsEditModalComponent,
  ],
  providers: [DeploymentService, EnvironmentService],
})
export class DeploymentsModule {}
