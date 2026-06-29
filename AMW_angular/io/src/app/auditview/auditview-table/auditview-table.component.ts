import { Component, effect, QueryList, ViewChildren, inject, input } from '@angular/core';
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
  imports: [FormsModule, SortableHeader, NgbHighlight, AsyncPipe, DatePipe, NewlineFilterPipe],
})
export class AuditviewTableComponent {
  readonly auditlogEntries = input.required<AuditLogEntry[]>();

  service = inject(AuditviewTableService);
  auditlogEntries$: Observable<AuditLogEntry[]> = this.service.result$;
  @ViewChildren(SortableHeader) headers!: QueryList<SortableHeader>;
  dateFormat = DATE_TIME_FORMAT;

  constructor() {
    effect(() => {
      this.service.auditLogEntries = this.auditlogEntries();
    });
  }

  onSort({ column, direction }: SortEvent) {
    this.headers.forEach((header) => {
      if (header.sortable() !== column) {
        header.direction.set('');
      }
    });
    this.service.sortColumn = column;
    this.service.sortDirection = direction;
  }
}
