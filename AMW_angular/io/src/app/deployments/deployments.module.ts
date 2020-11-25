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
import { LogsComponent } from './logs/logs.component';
import { DeploymentContainerComponent } from './deployment-container/deployment-container.component';
import { NgbModule } from '@ng-bootstrap/ng-bootstrap';

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    DeploymentsRoutingModule,
    SharedModule,
    NgbModule,
  ],
  declarations: [
    DeploymentsComponent,
    DeploymentsListComponent,
    DeploymentsEditModalComponent,
    LogsComponent,
    DeploymentContainerComponent,
  ],
  providers: [DeploymentService, EnvironmentService],
})
export class DeploymentsModule {}
