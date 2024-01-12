import { Component, OnInit } from '@angular/core';
import { AsyncPipe, DatePipe, NgFor, NgIf } from '@angular/common';
import { LoadingIndicatorComponent } from '../../shared/elements/loading-indicator.component';
import { BehaviorSubject, Observable } from 'rxjs';
import { HttpClient } from '@angular/common/http';
import { IconComponent } from '../../shared/icon/icon.component';
import { PaginationComponent } from '../../shared/pagination/pagination.component';
import { ToastComponent } from 'src/app/shared/elements/toast/toast.component';
import { DATE_FORMAT } from '../../core/amw-constants';
import { ReleaseEditComponent } from './release-edit.component';
import { Release } from './release';
import { ReleasesService } from './ReleasesService';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';

@Component({
  selector: 'amw-releases',
  standalone: true,
  imports: [
    AsyncPipe,
    DatePipe,
    IconComponent,
    LoadingIndicatorComponent,
    NgIf,
    NgFor,
    PaginationComponent,
    ReleaseEditComponent,
    ToastComponent,
  ],
  templateUrl: './releases.component.html',
})
export class ReleasesComponent implements OnInit {
  releases$: Observable<Release[]>;
  defaultRelease$: Observable<Release>;
  isLoading$: Observable<boolean> = new BehaviorSubject<boolean>(true);
  results$: BehaviorSubject<Release[]> = new BehaviorSubject<Release[]>([]);
  defaultReleaseId: number;

  dateFormat = DATE_FORMAT;

  selectedRelease: Release;

  // pagination with default values
  maxResults: number = 10;
  offset: number = 0;
  allResults: number;
  currentPage: number;
  lastPage: number;

  isLoading: boolean = true;
  edit: boolean = false;

  hasPermissionToCreateRelease: boolean = false;

  errorMessage: string = ''; //TODO handle it

  constructor(
    private http: HttpClient,
    private modalService: NgbModal,
    private releasesService: ReleasesService,
  ) {}

  ngOnInit(): void {
    this.canCreateRelease();
    this.getDefaultRelease();
    this.getReleases();
  }

  private async getDefaultRelease() {
    this.defaultRelease$ = this.http.get<Release>('/AMW_rest/resources/releases/default');
    this.defaultRelease$.subscribe({
      next: (result) => {
        this.defaultReleaseId = result.id;
      },
    });
  }

  private getReleases() {
    this.isLoading = true;
    this.releases$ = this.http.get<Release[]>(
      '/AMW_rest/resources/releases?start=' + this.offset + '&limit=' + this.maxResults,
    );

    this.releases$.subscribe({
      next: (results) => {
        results.map((release) =>
          release.id === this.defaultReleaseId ? (release.default = true) : (release.default = false),
        );
        this.results$.next(results);
        this.allResults = results.length;
        this.currentPage = Math.floor(this.offset / this.maxResults) + 1;
        this.lastPage = Math.ceil(this.allResults / this.maxResults);
        this.isLoading = false;
      },

      complete: () => (this.isLoading = false),
    });
  }

  private canCreateRelease() {
    this.releasesService.canCreateRelease().subscribe({
      next: (r) => (this.hasPermissionToCreateRelease = r),
      error: (e) => (this.errorMessage = e),
    });
  }

  addRelease() {
    const modalRef = this.modalService.open(ReleaseEditComponent);
    modalRef.componentInstance.selectedRelease = undefined;
    modalRef.componentInstance.saveRelease.subscribe((release: Release) => this.save(release));
  }

  editRelease(id: number) {}

  save(release: Release) {
    this.isLoading = true;
    this.releasesService.create(release).subscribe({
      next: (r) => r,
      error: (e) => (this.errorMessage = this.errorMessage ? this.errorMessage + '<br>' + e : e),

      complete: () => this.getReleases(),
    });
    this.isLoading = false;
    // TODO           Toast it
  }

  deleteRelease(id: number) {}

  setMaxResultsPerPage(max: number) {
    this.maxResults = max;
    this.offset = 0;
    this.getReleases();
  }

  setNewOffset(offset: number) {
    this.offset = offset;
    this.getReleases();
  }
}
