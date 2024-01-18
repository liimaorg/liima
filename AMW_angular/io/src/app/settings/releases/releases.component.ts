import { Component, OnInit } from '@angular/core';
import { AsyncPipe, DatePipe, NgFor, NgIf } from '@angular/common';
import { LoadingIndicatorComponent } from '../../shared/elements/loading-indicator.component';
import { BehaviorSubject, combineLatestWith, filter, Observable, switchMap, take } from 'rxjs';
import { HttpClient } from '@angular/common/http';
import { IconComponent } from '../../shared/icon/icon.component';
import { PaginationComponent } from '../../shared/pagination/pagination.component';
import { ToastComponent } from 'src/app/shared/elements/toast/toast.component';
import { DATE_FORMAT } from '../../core/amw-constants';
import { ReleaseEditComponent } from './release-edit.component';
import { Release } from './release';
import { ReleasesService } from './ReleasesService';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { AuthService } from '../../auth/auth.service';
import { withLatestFrom, debounceTime, map, tap } from 'rxjs/operators';

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
  providers: [AuthService],
  templateUrl: './releases.component.html',
})
export class ReleasesComponent implements OnInit {
  releases$: Observable<Release[]>;
  defaultRelease$: Observable<Release>;
  isLoading$: Observable<boolean> = new BehaviorSubject<boolean>(true);
  results$: BehaviorSubject<Release[]> = new BehaviorSubject<Release[]>([]);

  dateFormat = DATE_FORMAT;

  selectedRelease: Release;

  // pagination with default values
  maxResults: number = 10;
  offset: number = 0;
  allResults: number;
  currentPage: number;
  lastPage: number;

  isLoading: boolean = true;

  canCreate: boolean = false;
  canEdit: boolean = false;
  canDelete: boolean = false;

  errorMessage: string = ''; //TODO handle it

  constructor(
    private authService: AuthService,
    private http: HttpClient,
    private modalService: NgbModal,
    private releasesService: ReleasesService,
  ) {}

  ngOnInit(): void {
    this.getUserPermissions();
    this.getDefaultRelease();
    this.getReleases();
  }

  private getUserPermissions() {
    this.authService.getActionsForPermission('RELEASE').subscribe((value) => {
      this.canCreate = value.indexOf('CREATE') > -1;
      this.canEdit = value.indexOf('UPDATE') > -1;
      this.canDelete = value.indexOf('DELETE') > -1;
    });
  }

  private getDefaultRelease() {
    this.defaultRelease$ = this.http.get<Release>('/AMW_rest/resources/releases/default');
  }

  private getReleases() {
    this.isLoading = true;
    this.releases$ = this.http.get<Release[]>(
      '/AMW_rest/resources/releases?start=' + this.offset + '&limit=' + this.maxResults,
    );

    this.releases$
      .pipe(
        withLatestFrom(this.defaultRelease$, (releases, defaultR) => ({ releases, defaultR })),
        map(({ releases, defaultR }) =>
          releases.map((release) => {
            if (release.id === defaultR.id) {
              release.default = true;
            }
            return release;
          }),
        ),
      )
      .subscribe((results) => {
        this.results$.next(results);
        this.allResults = results.length;
        this.currentPage = Math.floor(this.offset / this.maxResults) + 1;
        this.lastPage = Math.ceil(this.allResults / this.maxResults);
        this.isLoading = false;
      });
  }

  addRelease() {
    const modalRef = this.modalService.open(ReleaseEditComponent);
    const newRelease: Release = {
      default: false,
      description: '',
      id: 0,
      installationInProductionAt: undefined,
      mainRelease: false,
      name: '',
    };

    modalRef.componentInstance.release = newRelease;
    modalRef.componentInstance.saveRelease.subscribe((release: Release) => this.save(release));
  }

  editRelease(release: Release) {
    const modalRef = this.modalService.open(ReleaseEditComponent);
    modalRef.componentInstance.release = release;
    modalRef.componentInstance.saveRelease.subscribe((release: Release) => this.save(release));
  }

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

  deleteRelease(release: Release) {}

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
