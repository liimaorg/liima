import { Component, computed, inject, OnInit, Signal } from '@angular/core';
import { BehaviorSubject, Subject } from 'rxjs';
import { LoadingIndicatorComponent } from '../shared/elements/loading-indicator.component';
import { AsyncPipe } from '@angular/common';
import { IconComponent } from '../shared/icon/icon.component';
import { PageComponent } from '../layout/page/page.component';
import { AppsFilterComponent } from './apps-filter/apps-filter.component';
import { AuthService } from '../auth/auth.service';
import { ReleasesService } from '../settings/releases/releases.service';
import { AppsService } from './apps.service';
import { Release } from '../settings/releases/release';
import { takeUntil } from 'rxjs/operators';
import { ToastService } from '../shared/elements/toast/toast.service';
import { AppServer } from './app-server';
import { AppsServersListComponent } from './apps-servers-list/apps-servers-list.component';
import { PaginationComponent } from '../shared/pagination/pagination.component';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { AppServerAddComponent } from './app-server-add/app-server-add.component';
import { toSignal } from '@angular/core/rxjs-interop';
import { AppAddComponent } from './app-add/app-add.component';
import { ResourceService } from '../resource/resource.service';
import { Resource } from '../resource/resource';
import { AppCreate } from './app-create';

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
  ],
  templateUrl: './apps.component.html',
})
export class AppsComponent implements OnInit {
  private appsService = inject(AppsService);
  private authService = inject(AuthService);
  private modalService = inject(NgbModal);
  private releaseService = inject(ReleasesService); // getCount -> getReleases(0, count)
  private resourceService = inject(ResourceService);
  private toastService = inject(ToastService);

  releases: Signal<Release[]> = toSignal(this.releaseService.getReleases(0, 50), { initialValue: [] as Release[] });
  appServerGroups = toSignal(this.resourceService.getByType('APPLICATIONSERVER'), {
    initialValue: [] as Resource[],
  });
  appServers = this.appsService.apps;
  count = this.appsService.count;
  maxResults = this.appsService.limit;
  offset = this.appsService.offset;
  filter = this.appsService.filter;
  releaseId = this.appsService.releaseId;
  private error$ = new BehaviorSubject<string>('');
  private destroy$ = new Subject<void>();

  canCreateApp = false;
  canCreateAppServer = false;
  canViewAppList = false;
  isLoading = false;

  currentPage: number;
  lastPage: number;

  loadingPermissions = computed(() => {
    if (this.authService.restrictions().length > 0) {
      this.getUserPermissions();
    } else {
      return `<div>Could not load permissions</div>`;
    }
  });

  ngOnInit(): void {
    this.error$.pipe(takeUntil(this.destroy$)).subscribe((msg) => {
      msg !== '' ? this.toastService.error(msg) : null;
    });
    this.isLoading = true;
    this.setPagination();
    this.isLoading = false;
  }

  private setPagination() {
    this.currentPage = Math.floor(this.offset() / this.maxResults()) + 1;
    this.lastPage = Math.ceil(this.count() / this.maxResults());
  }

  private getUserPermissions() {
    this.canCreateApp = this.authService.hasResourcePermission('RESOURCE', 'CREATE', 'APPLICATION');
    this.canCreateAppServer = this.authService.hasResourcePermission('RESOURCE', 'CREATE', 'APPLICATIONSERVER');
    this.canViewAppList = this.authService.hasPermission('APP_AND_APPSERVER_LIST', 'READ');
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
    this.isLoading = true;
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
          this.isLoading = false;
        },
      });
  }

  saveApp(app: AppCreate) {
    this.isLoading = true;
    this.appsService
      .createApp(app)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => this.toastService.success('App saved successfully.'),
        error: (e) => this.error$.next(e.toString()),
        complete: () => {
          this.appsService.refreshData();
          this.isLoading = false;
        },
      });
  }

  setMaxResultsPerPage(max: number) {
    this.maxResults.set(max);
    this.offset.set(0);
    this.appsService.refreshData();
    this.setPagination();
  }

  setNewOffset(offset: number) {
    this.offset.set(offset);
    this.appsService.refreshData();
    this.setPagination();
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
}
