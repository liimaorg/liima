import {
  Component,
  QueryList,
  ViewChildren,
  OnChanges,
  SimpleChanges,
  Input
} from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import { AuditLogEntry } from '../auditview-entry';
import { AuditviewTableService } from '../auditview-table.service';
import { AuditviewService } from '../auditview.service';
import { SortableHeader, SortEvent } from '../sortable.directive';

@Component({
  selector: 'app-auditview-table',
  templateUrl: './auditview-table.component.html',
  styleUrls: ['./auditview-table.component.scss'],
  providers: [AuditviewService]
})
export class AuditviewTableComponent implements OnChanges {
  @Input() auditlogEntries;

  auditlogEntries$: Observable<AuditLogEntry[]>;
  total$: Observable<number>;
  resourceId: number;
  @ViewChildren(SortableHeader) headers: QueryList<SortableHeader>;

  constructor(
    public service: AuditviewTableService,
    private auditviewservice: AuditviewService
  ) {
    this.auditlogEntries$ = service.result$;
  }

  ngOnChanges(changes: SimpleChanges): void {
    // todo: set auditlogentries in service...
    if (changes.auditlogEntries) {
      this.service.auditLogEntries = changes.auditlogEntries.currentValue;
    }
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
