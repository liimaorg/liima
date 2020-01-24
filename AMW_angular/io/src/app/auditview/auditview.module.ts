import { NgModule } from '@angular/core';
import { CommonModule, DecimalPipe, DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuditviewComponent } from './auditview.component';
import { AuditviewRoutingModule } from './auditview-routing.module';
import { AuditviewService } from './auditview.service';
import { NewlineFilterPipe } from '../customfilter/newlineFilterPipe';
import { AuditviewFilterPipe } from '../customfilter/auditviewFilterPipe';
import { AuditviewTableComponent } from './auditview-table/auditview-table.component';
import { NgbModule } from '@ng-bootstrap/ng-bootstrap';
import { SortableHeader } from './sortable.directive';
import { BaseModule } from '../base/base.module';

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    AuditviewRoutingModule,
    NgbModule,
    BaseModule
  ],
  declarations: [
    AuditviewComponent,
    NewlineFilterPipe,
    AuditviewFilterPipe,
    AuditviewTableComponent,
    SortableHeader
  ],
  providers: [AuditviewService, DatePipe]
})
export class AuditviewModule {}
