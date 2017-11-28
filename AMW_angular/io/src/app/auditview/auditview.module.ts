import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuditviewComponent } from './auditview.component';
import { DeploymentService } from './deployment.service';
import { AuditviewRoutingModule } from './auditview-routing.module';
import { AuditviewService } from "./auditview.service";

@NgModule({
  imports: [CommonModule, FormsModule, AuditviewRoutingModule],
  declarations: [AuditviewComponent],
  providers: [AuditviewService]
})
export class AuditviewModule {
}
