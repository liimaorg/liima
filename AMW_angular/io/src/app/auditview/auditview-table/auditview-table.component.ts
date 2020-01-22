import { Component, OnInit, ViewChildren, QueryList } from '@angular/core';
import { Observable, pipe } from 'rxjs';
import { AuditLogEntry } from '../auditview-entry';
import { AuditviewService } from '../auditview.service';
import { SortEvent, SortableHeader } from '../sortable.directive';
import { ActivatedRoute, ParamMap } from '@angular/router';
import { AuditviewTableService } from '../auditview-table.service';
import { switchMap } from 'rxjs/operators';

@Component({
  selector: 'app-auditview-table',
  templateUrl: './auditview-table.component.html',
  styleUrls: ['./auditview-table.component.scss'],
  providers: [AuditviewService]
})
export class AuditviewTableComponent {
  auditlogEntries$: Observable<AuditLogEntry[]>;
  total$: Observable<number>;
  resourceId: number;

  @ViewChildren(SortableHeader) headers: QueryList<SortableHeader>;

  constructor(
    private service: AuditviewTableService,
    private auditviewservice: AuditviewService,
    private route: ActivatedRoute
  ) {
    // TODO: use a path param instead of a query param and clean it up...
    this.route.queryParams.subscribe((param: any) => {
      if (param['resourceId']) {
        try {
          this.resourceId = JSON.parse(param['resourceId']);
        } catch (e) {
          console.error(e);
        }
      }
    });

    if (this.resourceId) {
      this.auditviewservice
        .getAuditLogForResource(this.resourceId)
        .subscribe(auditlogEntries =>
          service.setAuditlogEntries(auditlogEntries)
        );
    }
    this.auditlogEntries$ = service.auditlogEntries$;
    this.total$ = service.total$;
  }

  onSort({ column, direction }: SortEvent) {
    this.headers.forEach(header => {
      if (header.sortable !== column) {
        header.direction = '';
      }
    });
    this.service.sortColumn = column;
    this.service.sortDirection = direction;
  }
}
