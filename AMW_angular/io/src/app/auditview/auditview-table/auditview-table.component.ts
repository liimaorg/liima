import { Component, Input, OnChanges, QueryList, SimpleChanges, ViewChildren } from '@angular/core';
import { Observable } from 'rxjs';
import { AuditLogEntry } from '../auditview-entry';
import { AuditviewTableService } from './auditview-table.service';
import { SortableHeader, SortEvent } from './sortable.directive';
import { DATE_TIME_FORMAT } from '../../core/amw-constants';
import { NewlineFilterPipe } from './newlineFilterPipe';
import { NgbHighlight } from '@ng-bootstrap/ng-bootstrap';
import { AsyncPipe, DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
    selector: 'app-auditview-table',
    templateUrl: './auditview-table.component.html',
    styleUrls: ['./auditview-table.component.scss'],
    imports: [FormsModule, SortableHeader, NgbHighlight, AsyncPipe, DatePipe, NewlineFilterPipe]
})
export class AuditviewTableComponent implements OnChanges {
  @Input() auditlogEntries;

  auditlogEntries$: Observable<AuditLogEntry[]>;
  @ViewChildren(SortableHeader) headers: QueryList<SortableHeader>;
  dateFormat = DATE_TIME_FORMAT;

  constructor(public service: AuditviewTableService) {
    this.auditlogEntries$ = service.result$;
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes.auditlogEntries) {
      this.service.auditLogEntries = changes.auditlogEntries.currentValue;
    }
  }

  onSort({ column, direction }: SortEvent) {
    this.headers.forEach((header) => {
      if (header.sortable !== column) {
        header.direction = '';
      }
    });
    this.service.sortColumn = column;
    this.service.sortDirection = direction;
  }
}
