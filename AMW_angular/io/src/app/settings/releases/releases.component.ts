import { Component, computed, inject, OnDestroy, signal, Signal } from '@angular/core';
import { LoadingIndicatorComponent } from '../../shared/elements/loading-indicator.component';
import { BehaviorSubject, Subject } from 'rxjs';
import { IconComponent } from '../../shared/icon/icon.component';
import { PaginationComponent } from '../../shared/pagination/pagination.component';
import { ReleaseEditComponent } from './release-edit.component';
import { Release } from './release';
import { ReleasesService } from './releases.service';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { AuthService } from '../../auth/auth.service';
import { takeUntil } from 'rxjs/operators';
import { ReleaseDeleteComponent } from './release-delete.component';
import { ToastService } from '../../shared/elements/toast/toast.service';
import { ButtonComponent } from '../../shared/button/button.component';
import { toSignal } from '@angular/core/rxjs-interop';
import { TableComponent, TableColumnType } from '../../shared/table/table.component';
import { Action } from 'src/app/auth/restriction';

@Component({
  selector: 'app-releases',
  imports: [IconComponent, LoadingIndicatorComponent, PaginationComponent, ButtonComponent, TableComponent],
  templateUrl: './releases.component.html',
})
export class ReleasesComponent implements OnDestroy {
  private authService = inject(AuthService);
  private modalService = inject(NgbModal);
  private releasesService = inject(ReleasesService);
  private toastService = inject(ToastService);

  releases: Signal<Release[]> = this.releasesService.releases;
  defaultRelease: Signal<Release> = toSignal(this.releasesService.getDefaultRelease(), {
    initialValue: null as Release,
  });
  count: Signal<number> = toSignal(this.releasesService.getCount(), { initialValue: null as number });

  private error$ = new BehaviorSubject<string>('');
  private destroy$ = new Subject<void>();

  isLoading = signal(false);

  // pagination with default values
  maxResults = signal(10);
  offset = signal(0);

  currentPage: Signal<number> = computed(() => Math.floor(this.offset() / this.maxResults()) + 1);
  lastPage: Signal<number> = computed(() => Math.ceil(this.count() / this.maxResults()));

  results: Signal<Release[]> = computed(() => {
    return this.releases().map((release) => ({
      ...release,
      default: release.id === this.defaultRelease()?.id,
    }));
  });

  resultTableData = computed(() =>
    this.results().map((release) => {
      return {
        id: release.id,
        name: release.name,
        mainRelease: release.mainRelease,
        description: release.description,
        installationInProductionAt: release.installationInProductionAt,
        readonly: release.default,
      };
    }),
  );

  permissions = computed(() => {
    if (this.authService.restrictions().length > 0) {
      return {
        canCreate: this.authService.hasPermission('RELEASE', Action.CREATE),
        canEdit: this.authService.hasPermission('RELEASE', Action.UPDATE),
        canDelete: this.authService.hasPermission('RELEASE', Action.DELETE),
      };
    } else {
      return {
        canCreate: false,
        canEdit: false,
        canDelete: false,
      };
    }
  });

  ngOnDestroy(): void {
    this.destroy$.next(undefined);
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

  editRelease(releaseId: number) {
    const modalRef = this.modalService.open(ReleaseEditComponent);
    modalRef.componentInstance.release = this.results().find((item) => item.id === releaseId);
    modalRef.componentInstance.saveRelease
      .pipe(takeUntil(this.destroy$))
      .subscribe((release: Release) => this.save(release));
  }

  save(release: Release) {
    this.isLoading.set(true);
    this.releasesService
      .save(release)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => this.toastService.success('Release saved successfully.'),
        error: (e) => this.error$.next(e),
        complete: () =>
          this.releasesService.setOffsetAndMaxResultsForReleases({
            offset: this.offset(),
            maxResults: this.maxResults(),
          }),
      });
    this.isLoading.set(false);
  }

  deleteRelease(releaseId: number) {
    this.releasesService
      .getReleaseResources(releaseId)
      .pipe(takeUntil(this.destroy$))
      .subscribe((list) => {
        const modalRef = this.modalService.open(ReleaseDeleteComponent);
        modalRef.componentInstance.release = this.results().find((item) => item.id === releaseId);
        modalRef.componentInstance.resources = list;
        modalRef.componentInstance.deleteRelease
          .pipe(takeUntil(this.destroy$))
          .subscribe((release: Release) => this.delete(release));
      });
  }

  delete(release: Release) {
    this.isLoading.set(true);
    this.releasesService
      .delete(release.id)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => this.toastService.success('Release deleted.'),
        error: (e) => this.error$.next(e),
        complete: () =>
          this.releasesService.setOffsetAndMaxResultsForReleases({
            offset: this.offset(),
            maxResults: this.maxResults(),
          }),
      });
    this.isLoading.set(false);
  }

  setMaxResultsPerPage(max: number) {
    this.maxResults.set(max);
    this.offset.set(0);
    this.releasesService.setOffsetAndMaxResultsForReleases({ offset: this.offset(), maxResults: this.maxResults() });
  }

  setNewOffset(offset: number) {
    this.offset.set(offset);
    this.releasesService.setOffsetAndMaxResultsForReleases({ offset: this.offset(), maxResults: this.maxResults() });
  }

  releasesHeader(): TableColumnType<{
    id: number;
    name: string;
    mainRelease: boolean;
    description: string;
    installationInProductionAt: number;
    readonly: boolean;
  }>[] {
    return [
      {
        key: 'name',
        columnTitle: 'Release Name',
      },
      {
        key: 'mainRelease',
        columnTitle: 'Main Release',
      },
      {
        key: 'description',
        columnTitle: 'Description',
      },
      {
        key: 'installationInProductionAt',
        columnTitle: 'Date',
        cellType: 'date',
      },
    ];
  }
}
