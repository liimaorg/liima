import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { DeploymentComponent } from './deployment.component';
import { DeploymentService } from './deployment.service';
import { EnvironmentService } from './environment.service';
import { DeploymentRoutingModule } from './deployment-routing.module';
import { SharedModule } from '../shared/shared.module';
import { NgbModule } from '@ng-bootstrap/ng-bootstrap';

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    DeploymentRoutingModule,
    SharedModule,
    NgbModule,
  ],
  declarations: [DeploymentComponent],
  providers: [DeploymentService, EnvironmentService],
})
export class DeploymentModule {}
