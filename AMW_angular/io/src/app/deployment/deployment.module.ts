import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { DeploymentComponent } from './deployment.component';
import { DeploymentService } from './deployment.service';
import { EnvironmentService } from './environment.service';
import { DeploymentRoutingModule } from './deployment-routing.module';

@NgModule({
  imports: [CommonModule, FormsModule, DeploymentRoutingModule],
  declarations: [DeploymentComponent],
  providers: [DeploymentService, EnvironmentService]
})
export class DeploymentModule {
}
