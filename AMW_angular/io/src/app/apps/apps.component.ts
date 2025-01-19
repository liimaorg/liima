import { ChangeDetectionStrategy, Component, computed, inject, OnDestroy, OnInit, signal, Signal } from '@angular/core';
import { BehaviorSubject, of, skip, Subject, take } from 'rxjs';
import { LoadingIndicatorComponent } from '../shared/elements/loading-indicator.component';
import { AsyncPipe } from '@angular/common';
import { IconComponent } from '../shared/icon/icon.component';
import { PageComponent } from '../layout/page/page.component';
import { AppsFilterComponent } from './apps-filter/apps-filter.component';
import { AuthService } from '../auth/auth.service';
import { ReleasesService } from '../settings/releases/releases.service';
import { AppsService } from './apps.service';
import { Release } from '../settings/releases/release';
import { switchMap, takeUntil } from 'rxjs/operators';
import { ToastService } from '../shared/elements/toast/toast.service';
import { AppServer } from './app-server';
import { AppsServersListComponent } from './apps-servers-list/apps-servers-list.component';
import { PaginationComponent } from '../shared/pagination/pagination.component';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { AppServerAddComponent } from './app-server-add/app-server-add.component';
import { toObservable, toSignal } from '@angular/core/rxjs-interop';
import { AppAddComponent } from './app-add/app-add.component';
import { ResourceService } from '../resource/resource.service';
import { Resource } from '../resource/resource';
import { AppCreate } from './app-create';
import { ButtonComponent } from '../shared/button/button.component';
import { ResourceTypesService } from '../resource/resource-types.service';

@Component({
  selector: 'app-apps',
  standalone: true,
  imports: [
    AppsFilterComponent,
    AppsServersListComponent,
    AsyncPipe,
    IconComponent,
    LoadingIndicatorComponent,
    PageComponent,
    PaginationComponent,
    ButtonComponent,
  ],
  templateUrl: './apps.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AppsComponent implements OnInit, OnDestroy {
  private appsService = inject(AppsService);
  private authService = inject(AuthService);
  private modalService = inject(NgbModal);
  private releaseService = inject(ReleasesService); // getCount -> getReleases(0, count)
  private resourceService = inject(ResourceService);
  private resourceTypesService = inject(ResourceTypesService);
  private toastService = inject(ToastService);

  upcomingRelease: Signal<Release> = toSignal(this.releaseService.getUpcomingRelease());

  releases: Signal<Release[]> = toSignal(this.releaseService.getReleases(0, 50), { initialValue: [] as Release[] });
  appServerResourceType$ = this.resourceTypesService.getResourceTypeByName('APPLICATIONSERVER');
  appServerGroups = toSignal(
    this.appServerResourceType$.pipe(
      switchMap((resourceType) => (resourceType ? this.resourceService.getGroupsForType(resourceType) : of([]))),
    ),
    { initialValue: [] as Resource[] },
  );
  appServers = this.appsService.apps;
  count = this.appsService.count;
  maxResults = this.appsService.limit;
  offset = this.appsService.offset;
  filter = this.appsService.filter;
  releaseId = this.appsService.releaseId;
  private error$ = new BehaviorSubject<string>('');
  private destroy$ = new Subject<void>();

  showLoader = signal(false);
  isLoading = computed(() => {
    return this.appServers() === undefined || this.showLoader();
  });

  permissions = computed(() => {
    if (this.authService.restrictions().length > 0) {
      return {
        canCreateApp: this.authService.hasResourcePermission('RESOURCE', 'CREATE', 'APPLICATION'),
        canCreateAppServer: this.authService.hasResourcePermission('RESOURCE', 'CREATE', 'APPLICATIONSERVER'),
        canViewAppList: this.authService.hasPermission('APP_AND_APPSERVER_LIST', 'READ'),
      };
    } else {
      return { canCreateApp: false, canCreateAppServer: false, canViewAppList: false };
    }
  });

  currentPage = computed(() => Math.floor(this.offset() / this.maxResults()) + 1);
  lastPage = computed(() => Math.ceil(this.count() / this.maxResults()));

  constructor() {
    toObservable(this.upcomingRelease)
      .pipe(takeUntil(this.destroy$), skip(1), take(1))
      .subscribe((release) => {
        this.releaseId.set(release.id);
        this.appsService.refreshData();
      });
  }

  ngOnInit(): void {
    this.error$.pipe(takeUntil(this.destroy$)).subscribe((msg) => {
      msg !== '' ? this.toastService.error(msg) : null;
    });
  }

  addApp() {
    const modalRef = this.modalService.open(AppAddComponent);
    modalRef.componentInstance.releases = this.releases;
    modalRef.componentInstance.appServerGroups = this.appServerGroups;
    modalRef.componentInstance.saveApp.pipe(takeUntil(this.destroy$)).subscribe((app: AppCreate) => this.saveApp(app));
  }

  addServer() {
    const modalRef = this.modalService.open(AppServerAddComponent);
    modalRef.componentInstance.releases = this.releases;
    modalRef.componentInstance.saveAppServer
      .pipe(takeUntil(this.destroy$))
      .subscribe((appServer: AppServer) => this.saveAppServer(appServer));
  }

  saveAppServer(appServer: AppServer) {
    this.showLoader.set(true);
    this.appsService
      .createAppServer(appServer)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          this.toastService.success('AppServer saved successfully.');
        },
        error: (e) => {
          this.error$.next(e.toString());
        },
        complete: () => {
          this.appsService.refreshData();
        },
      });
  }

  saveApp(app: AppCreate) {
    this.showLoader.set(true);
    this.appsService
      .createApp(app)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => this.toastService.success('App saved successfully.'),
        error: (e) => this.error$.next(e.toString()),
        complete: () => {
          this.appsService.refreshData();
        },
      });
  }

  setMaxResultsPerPage(max: number) {
    this.maxResults.set(max);
    this.offset.set(0);
    this.appsService.refreshData();
  }

  setNewOffset(offset: number) {
    this.offset.set(offset);
    this.appsService.refreshData();
  }

  updateFilter(values: { filter: string; releaseId: number }) {
    let update = false;
    if (values.filter !== undefined && this.filter() !== values.filter) {
      this.filter.set(values.filter);
      update = true;
    }

    if (values.releaseId > 0 && this.releaseId() !== values.releaseId) {
      this.releaseId.set(values.releaseId);
      update = true;
    }
    if (update) {
      this.appsService.refreshData();
    }
  }

  ngOnDestroy(): void {
    this.destroy$.next(undefined);
  }
}
