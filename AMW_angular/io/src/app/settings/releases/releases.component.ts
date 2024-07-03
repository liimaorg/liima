import { Component, OnInit } from '@angular/core';
import { AsyncPipe, DatePipe, NgFor, NgIf } from '@angular/common';
import { LoadingIndicatorComponent } from '../../shared/elements/loading-indicator.component';
import { BehaviorSubject, combineLatest, Observable, Subject } from 'rxjs';
import { HttpClient } from '@angular/common/http';
import { IconComponent } from '../../shared/icon/icon.component';
import { PaginationComponent } from '../../shared/pagination/pagination.component';
import { DATE_FORMAT } from '../../core/amw-constants';
import { ReleaseEditComponent } from './release-edit.component';
import { Release } from './release';
import { ReleasesService } from './releases.service';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { AuthService } from '../../auth/auth.service';
import { map, takeUntil } from 'rxjs/operators';
import { ReleaseDeleteComponent } from './release-delete.component';
import { ToastService } from '../../shared/elements/toast/toast.service';

@Component({
  selector: 'app-releases',
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
    ReleaseDeleteComponent,
  ],
  providers: [AuthService],
  templateUrl: './releases.component.html',
})
export class ReleasesComponent implements OnInit {
  releases$: Observable<Release[]>;
  defaultRelease$: Observable<Release>;
  count$: Observable<number>;
  results$: Observable<Release[]>;
  private error$ = new BehaviorSubject<string>('');

  private destroy$ = new Subject<void>();

  dateFormat = DATE_FORMAT;

  // pagination with default values
  maxResults = 10;
  offset = 0;
  allResults: number;
  currentPage: number;
  lastPage: number;

  isLoading = true;

  canCreate = false;
  canEdit = false;
  canDelete = false;

  constructor(
    private authService: AuthService,
    private http: HttpClient,
    private modalService: NgbModal,
    private releasesService: ReleasesService,
    private toastService: ToastService,
  ) {}

  ngOnInit(): void {
    this.error$.pipe(takeUntil(this.destroy$)).subscribe((msg) => {
      msg !== '' ? this.toastService.error(msg) : null;
    });
    this.getUserPermissions();
    this.count$ = this.releasesService.getCount();
    this.defaultRelease$ = this.releasesService.getDefaultRelease();
    this.getReleases();
  }

  ngOnDestroy(): void {
    this.destroy$.next(undefined);
  }

  private getUserPermissions() {
    this.authService
      .getActionsForPermission('RELEASE')
      .pipe(takeUntil(this.destroy$))
      .subscribe((value) => {
        if (value.indexOf('ALL') > -1) {
          this.canDelete = this.canEdit = this.canCreate = true;
        } else {
          this.canCreate = value.indexOf('CREATE') > -1;
          this.canEdit = value.indexOf('UPDATE') > -1;
          this.canDelete = value.indexOf('DELETE') > -1;
        }
      });
  }

  private getReleases() {
    this.isLoading = true;
    this.releases$ = this.releasesService.getReleases(this.offset, this.maxResults);
    this.currentPage = Math.floor(this.offset / this.maxResults) + 1;

    this.results$ = combineLatest([this.releases$, this.defaultRelease$, this.count$])
      .pipe(
        map(([releases, defaultR, count]) => {
          this.lastPage = Math.ceil(count / this.maxResults);
          releases.map((release) => {
            if (release.id === defaultR.id) {
              release.default = true;
            }
            return release;
          });
          return releases;
        }),
      )
      .pipe();

    this.isLoading = false;
  }

  addRelease() {
    const modalRef = this.modalService.open(ReleaseEditComponent);
    modalRef.componentInstance.release = {
      default: false,
      description: '',
      id: 0,
      installationInProductionAt: undefined,
      mainRelease: false,
      name: '',
    };
    modalRef.componentInstance.saveRelease
      .pipe(takeUntil(this.destroy$))
      .subscribe((release: Release) => this.save(release));
  }

  editRelease(release: Release) {
    const modalRef = this.modalService.open(ReleaseEditComponent);
    modalRef.componentInstance.release = release;
    modalRef.componentInstance.saveRelease
      .pipe(takeUntil(this.destroy$))
      .subscribe((release: Release) => this.save(release));
  }

  save(release: Release) {
    this.isLoading = true;
    this.releasesService
      .save(release)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (r) => r,
        error: (e) => this.error$.next(e),
        complete: () => {
          this.toastService.success('Release saved successfully.');
          this.getReleases();
        },
      });
    this.isLoading = false;
  }

  deleteRelease(release: Release) {
    this.releasesService
      .getReleaseResources(release.id)
      .pipe(takeUntil(this.destroy$))
      .subscribe((list) => {
        const modalRef = this.modalService.open(ReleaseDeleteComponent);
        modalRef.componentInstance.release = release;
        modalRef.componentInstance.resources = list;
        modalRef.componentInstance.deleteRelease
          .pipe(takeUntil(this.destroy$))
          .subscribe((release: Release) => this.delete(release));
      });
  }

  delete(release: Release) {
    this.isLoading = true;
    this.releasesService
      .delete(release.id)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (r) => r,
        error: (e) => this.error$.next(e),
        complete: () => {
          this.toastService.success('Release deleted.');
          this.getReleases();
        },
      });
    this.isLoading = false;
  }

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
