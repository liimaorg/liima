import { NgModule } from '@angular/core';
import { CommonModule, DecimalPipe, DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuditviewComponent } from './auditview.component';
import { AuditviewRoutingModule } from './auditview-routing.module';
import { AuditviewService } from './auditview.service';
import { NewlineFilterPipe } from '../customfilter/newlineFilterPipe';
import { AuditviewTableComponent } from './auditview-table/auditview-table.component';
import { NgbModule } from '@ng-bootstrap/ng-bootstrap';
import { SortableHeader } from './sortable.directive';
import { BaseModule } from '../base/base.module';
import { SharedModule } from '../shared/shared.module';

@NgModule({
  imports: [
    CommonModule,
    SharedModule,
    FormsModule,
    AuditviewRoutingModule,
    NgbModule,
    BaseModule
  ],
  declarations: [
    AuditviewComponent,
    NewlineFilterPipe,
    AuditviewTableComponent,
    SortableHeader
  ],
  providers: [AuditviewService, DatePipe]
})
export class AuditviewModule {}
