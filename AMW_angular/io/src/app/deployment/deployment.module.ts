import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { DeploymentComponent } from './deployment.component';
import { DeploymentService } from './deployment.service';
import { EnvironmentService } from './environment.service';
import { NgbDatepickerModule } from '@ng-bootstrap/ng-bootstrap';
import { NgSelectModule } from '@ng-select/ng-select';

@NgModule({
  imports: [CommonModule, FormsModule, NgbDatepickerModule, NgSelectModule, DeploymentComponent],
  providers: [DeploymentService, EnvironmentService],
})
export class DeploymentModule {}
