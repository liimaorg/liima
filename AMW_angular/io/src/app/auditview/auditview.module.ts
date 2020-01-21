import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuditviewComponent } from './auditview.component';
import { AuditviewRoutingModule } from './auditview-routing.module';
import { AuditviewService } from './auditview.service';
import { NewlineFilterPipe } from '../customfilter/newlineFilterPipe';
import { AuditviewFilterPipe } from '../customfilter/auditviewFilterPipe';
import {NgbModule} from "@ng-bootstrap/ng-bootstrap";

@NgModule({
  imports: [CommonModule, FormsModule, AuditviewRoutingModule, NgbModule],
  declarations: [AuditviewComponent, NewlineFilterPipe, AuditviewFilterPipe],
  providers: [AuditviewService]
})
export class AuditviewModule {
}
