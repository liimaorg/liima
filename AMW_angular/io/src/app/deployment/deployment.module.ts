import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { DeploymentComponent } from './deployment.component';
import { DeploymentService } from './deployment.service';
import { EnvironmentService } from './environment.service';
import { DeploymentRoutingModule } from './deployment-routing.module';
import { SharedModule } from '../shared/shared.module';
import { NgbDatepickerModule } from '@ng-bootstrap/ng-bootstrap';
import { NgSelectModule } from '@ng-select/ng-select';

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    DeploymentRoutingModule,
    SharedModule,
    NgbDatepickerModule,
    NgSelectModule
  ],
  declarations: [DeploymentComponent],
  providers: [DeploymentService, EnvironmentService],
})
export class DeploymentModule {}
