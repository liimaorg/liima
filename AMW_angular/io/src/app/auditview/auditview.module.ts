import { NgModule } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuditviewComponent } from './auditview.component';
import { AuditviewService } from './auditview.service';
import { AuditviewTableComponent } from './auditview-table/auditview-table.component';
import { NgbModule } from '@ng-bootstrap/ng-bootstrap';

import { NewlineFilterPipe } from './auditview-table/newlineFilterPipe';
import { SortableHeader } from './auditview-table/sortable.directive';

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    NgbModule,
    AuditviewComponent,
    NewlineFilterPipe,
    AuditviewTableComponent,
    SortableHeader,
  ],
  providers: [AuditviewService, DatePipe],
})
export class AuditviewModule {}
