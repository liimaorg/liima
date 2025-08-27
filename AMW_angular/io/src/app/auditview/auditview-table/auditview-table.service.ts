import { Injectable, inject } from '@angular/core';
import { BehaviorSubject, combineLatest, Observable, Subject } from 'rxjs';
import { debounceTime, map, tap } from 'rxjs/operators';
import { DatePipe } from '@angular/common';
import { AuditLogEntry } from '../auditview-entry';
import { SortDirection } from './sortable.directive';
import { DATE_TIME_FORMAT } from '../../core/amw-constants';

function sort(entries: AuditLogEntry[], column: string, direction: string): AuditLogEntry[] {
  if (direction === '') {
    return entries;
  } else {
    return [...entries].sort((a, b) => {
      const res = compare(a[column], b[column]);
      return direction === 'asc' ? res : -res;
    });
  }
}

function matches(entry: AuditLogEntry, term: string, pipe: DatePipe): boolean {
  const lowerCaseTerm = term.toLowerCase();
  return (
    pipe.transform(entry.timestamp, DATE_TIME_FORMAT).includes(term) ||
    nullSafeToLowerCase(entry.mode).includes(lowerCaseTerm) ||
    nullSafeToLowerCase(entry.editContextName).includes(lowerCaseTerm) ||
    nullSafeToLowerCase(entry.name).includes(lowerCaseTerm) ||
    nullSafeToLowerCase(entry.oldValue).includes(lowerCaseTerm) ||
    nullSafeToLowerCase(entry.relation).includes(lowerCaseTerm) ||
    nullSafeToLowerCase(entry.type).includes(lowerCaseTerm) ||
    nullSafeToLowerCase(entry.username).includes(lowerCaseTerm)
  );
}

// helper function to avoid nullpointer exceptions for values that are undefined.
function nullSafeToLowerCase(s: string) {
  return s ? s.toLowerCase() : '';
}

function compare(v1, v2) {
  return v1 < v2 ? -1 : v1 > v2 ? 1 : 0;
}

@Injectable({
  providedIn: 'root',
})
export class AuditviewTableService {
  private pipe = inject(DatePipe);

  private _loading$ = new BehaviorSubject<boolean>(true);

  private _result$: Observable<AuditLogEntry[]>;

  private searchTerm$ = new BehaviorSubject<string>('');
  private sortColumn$ = new BehaviorSubject<string>('timestamp');
  private sortDirection$ = new BehaviorSubject<SortDirection>('');
  private auditlogEntries$: Subject<AuditLogEntry[]> = new Subject();

  private _search$ = combineLatest([this.searchTerm$, this.sortColumn$, this.sortDirection$, this.auditlogEntries$]);

  constructor() {
    this._result$ = this._search$.pipe(
      tap(() => this._loading$.next(true)),
      debounceTime(200),
      map(([searchTerm, sortColumn, sortDirection, auditlogEntries]) => {
        return this._search(searchTerm, sortColumn, sortDirection, auditlogEntries);
      }),
      tap(() => this._loading$.next(false)),
    );
  }

  get result$() {
    return this._result$;
  }

  set auditLogEntries(entries: AuditLogEntry[]) {
    this.auditlogEntries$.next(entries);
  }
  set searchTerm(searchTerm: string) {
    this.searchTerm$.next(searchTerm);
  }

  set sortColumn(sortColumn: string) {
    this.sortColumn$.next(sortColumn);
  }
  set sortDirection(sortDirection: SortDirection) {
    this.sortDirection$.next(sortDirection);
  }

  private _search(
    searchTerm: string,
    sortColumn: string,
    sortDirection: SortDirection,
    auditlogEntries: AuditLogEntry[],
  ): AuditLogEntry[] {
    const auditviewEntries = sort(auditlogEntries, sortColumn, sortDirection);

    const result = auditviewEntries.filter((entry) => matches(entry, searchTerm, this.pipe));
    return result;
  }
}
