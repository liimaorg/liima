import { Injectable } from '@angular/core';
import { Observable, BehaviorSubject, Subject, of } from 'rxjs';
import { tap, debounceTime, switchMap } from 'rxjs/operators';
import { AuditLogEntry } from './auditview-entry';
import { SortDirection } from './sortable.directive';
import { DatePipe } from '@angular/common';

interface State {
  searchTerm: string;
  sortColumn: string;
  sortDirection: SortDirection;
}

interface SearchResult {
  auditviewEntries: AuditLogEntry[];
  total: number;
}

function sort(
  entries: AuditLogEntry[],
  column: string,
  direction: string
): AuditLogEntry[] {
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
    pipe.transform(entry.timestamp, 'yyyy-MM-dd HH:mm:ss').includes(term) ||
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
  providedIn: 'root'
})
export class AuditviewTableService {
  private _loading$ = new BehaviorSubject<boolean>(true);
  private _search$ = new Subject<void>();
  private _auditlogentries$ = new BehaviorSubject<AuditLogEntry[]>([]);
  private _total$ = new BehaviorSubject<number>(0);

  private _state: State = {
    searchTerm: '',
    sortColumn: '',
    sortDirection: ''
  };
  auditlogEntries: AuditLogEntry[] = [];

  constructor(private pipe: DatePipe) {
    this._search$
      .pipe(
        tap(() => this._loading$.next(true)),
        debounceTime(200),
        switchMap(() => this._search()),
        tap(() => this._loading$.next(false))
      )
      .subscribe(result => {
        this._auditlogentries$.next(result.auditviewEntries);
      });

    this._search$.next();
  }

  get auditlogEntries$() {
    return this._auditlogentries$.asObservable();
  }

  get total$() {
    return this._total$.asObservable();
  }

  get loading$() {
    return this._loading$.asObservable();
  }
  get searchTerm() {
    return this._state.searchTerm;
  }

  set searchTerm(searchTerm: string) {
    this._set({ searchTerm });
  }

  set sortColumn(sortColumn: string) {
    this._set({ sortColumn });
  }
  set sortDirection(sortDirection: SortDirection) {
    this._set({ sortDirection });
  }

  private _set(patch: Partial<State>) {
    Object.assign(this._state, patch);
    this._search$.next();
  }
  private _search(): Observable<SearchResult> {
    const { sortColumn, sortDirection, searchTerm } = this._state;

    // sort
    let auditviewEntries = sort(
      this.auditlogEntries,
      sortColumn,
      sortDirection
    );

    // filter
    auditviewEntries = auditviewEntries.filter(entry =>
      matches(entry, searchTerm, this.pipe)
    );
    const total = auditviewEntries.length;

    return of({ auditviewEntries, total });
  }

  setAuditlogEntries(entries: AuditLogEntry[]) {
    this.auditlogEntries = entries;
    this._search$.next();
  }
}
