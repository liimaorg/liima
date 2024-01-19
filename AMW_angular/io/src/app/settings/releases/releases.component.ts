import { AfterViewInit, Component, OnInit, ViewChild } from '@angular/core';
import { AsyncPipe, DatePipe, NgFor, NgIf } from '@angular/common';
import { LoadingIndicatorComponent } from '../../shared/elements/loading-indicator.component';
import { BehaviorSubject, Observable, Subject, take } from 'rxjs';
import { HttpClient } from '@angular/common/http';
import { IconComponent } from '../../shared/icon/icon.component';
import { PaginationComponent } from '../../shared/pagination/pagination.component';
import { ToastComponent } from 'src/app/shared/elements/toast/toast.component';
import { DATE_FORMAT } from '../../core/amw-constants';
import { ReleaseEditComponent } from './release-edit.component';
import { Release } from './release';
import { ReleasesService } from './releases.service';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { AuthService } from '../../auth/auth.service';
import { map, takeUntil, withLatestFrom } from 'rxjs/operators';
import { ReleaseDeleteComponent } from './release-delete.component';
import { ResourceEntity } from './resourceEntity';

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
    ReleaseDeleteComponent,
    ToastComponent,
  ],
  providers: [AuthService],
  templateUrl: './releases.component.html',
})
export class ReleasesComponent implements OnInit {
  releases$: Observable<Release[]>;
  defaultRelease$: Observable<Release>;
  count$: Observable<number>;
  isLoading$: Observable<boolean> = new BehaviorSubject<boolean>(true);
  results$: BehaviorSubject<Release[]> = new BehaviorSubject<Release[]>([]);
  private error$ = new BehaviorSubject<string>('');

  private destroy$ = new Subject<void>();

  dateFormat = DATE_FORMAT;

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

  @ViewChild(ToastComponent) toast: ToastComponent;

  constructor(
    private authService: AuthService,
    private http: HttpClient,
    private modalService: NgbModal,
    private releasesService: ReleasesService,
  ) {}

  ngOnInit(): void {
    this.error$.pipe(takeUntil(this.destroy$)).subscribe((msg) => {
      msg != '' ? this.toast.display(msg, 'error', 5000) : null;
    });
    this.getUserPermissions();
    this.getCount();
    this.defaultRelease$ = this.releasesService.getDefaultRelease().pipe(takeUntil(this.destroy$));
    this.getReleases();
  }

  ngOnDestroy(): void {
    this.destroy$.next(undefined);
  }

  private getCount() {
    this.releasesService
      .getCount()
      .pipe(takeUntil(this.destroy$))
      .subscribe((count) => (this.allResults = count));
  }

  private getUserPermissions() {
    this.authService
      .getActionsForPermission('RELEASE')
      // .pipe(takeUntil(this.destroy$))
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

    this.releases$
      .pipe(
        takeUntil(this.destroy$),
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
      .subscribe({
        next: (results) => {
          this.results$.next(results);
          this.currentPage = Math.floor(this.offset / this.maxResults) + 1;
          this.lastPage = Math.ceil(this.allResults / this.maxResults);
        },
        error: (e) => this.error$.next(e),
        complete: () => (this.isLoading = false),
      });
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
          this.toast.display('Release saved successfully.', 'success');
          this.getReleases();
        },
      });
    this.isLoading = false;
  }

  deleteRelease(release: Release) {
    let resources = {};
    // let resources: Map<string, ResourceEntity[]> = [
    //   [
    //     'APPLICATION',
    //     [
    //       {
    //         id: 267210,
    //         name: 'asdfasdf',
    //         resourceType: {
    //           id: 1,
    //           name: 'APPLICATION',
    //         },
    //       },
    //       {
    //         id: 266912,
    //         name: 'bert_beratung_service',
    //         resourceType: {
    //           id: 1,
    //           name: 'APPLICATION',
    //         },
    //       },
    //     ],
    //   ],
    // ];
    // this.releasesService
    //   .getReleaseResources(release.id)
    //   .pipe(takeUntil(this.destroy$))
    //   .subscribe((list: Map<string, ResourceEntity[]>) => (resources = list));

    const modalRef = this.modalService.open(ReleaseDeleteComponent);
    modalRef.componentInstance.release = release;
    modalRef.componentInstance.resources = resources;
    modalRef.componentInstance.deleteRelease
      .pipe(takeUntil(this.destroy$))
      .subscribe((release: Release) => this.delete(release));
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
          this.toast.display('Release deleted.', 'success');
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
