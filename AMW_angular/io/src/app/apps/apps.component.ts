import { ChangeDetectionStrategy, Component, computed, inject, OnDestroy, OnInit, signal, Signal } from '@angular/core';
import { BehaviorSubject, skip, Subject, take } from 'rxjs';
import { LoadingIndicatorComponent } from '../shared/elements/loading-indicator.component';
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
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { AppServerAddComponent } from './app-server-add/app-server-add.component';
import { toObservable, toSignal } from '@angular/core/rxjs-interop';
import { AppAddComponent } from './app-add/app-add.component';
import { ResourceService } from '../resource/resource.service';
import { AppCreate } from './app-create';
import { ButtonComponent } from '../shared/button/button.component';
import { ResourceTypesService } from '../resource/resource-types.service';
import { ActivatedRoute, Router } from '@angular/router';

@Component({
  selector: 'app-apps',
  imports: [
    AppsFilterComponent,
    AppsServersListComponent,
    IconComponent,
    LoadingIndicatorComponent,
    PageComponent,
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
  private router = inject(Router);
  private route = inject(ActivatedRoute);

  upcomingRelease: Signal<Release> = toSignal(this.releaseService.getUpcomingRelease());

  releases: Signal<Release[]> = toSignal(this.releaseService.getReleases(0, 50), { initialValue: [] as Release[] });
  appServerResourceType$ = this.resourceTypesService.getResourceTypeByName('APPLICATIONSERVER');
  appServerGroups = this.resourceService.resourceGroupListForType;
  appServers = this.appsService.apps;
  private error$ = new BehaviorSubject<string>('');
  private destroy$ = new Subject<void>();

  showLoader = signal(false);
  isLoading = computed(() => {
    return this.appServers() === undefined || this.appServers() === null || this.showLoader();
  });

  permissions = computed(() => {
    if (this.authService.restrictions().length > 0) {
      return {
        canCreateApp: this.authService.hasResourceTypePermission('RESOURCE', 'CREATE', 'APPLICATION'),
        canCreateAppServer: this.authService.hasResourceTypePermission('RESOURCE', 'CREATE', 'APPLICATIONSERVER'),
        canViewAppList: this.authService.hasPermission('APP_AND_APPSERVER_LIST', 'READ'),
      };
    } else {
      return { canCreateApp: false, canCreateAppServer: false, canViewAppList: false };
    }
  });

  constructor() {
    toObservable(this.upcomingRelease)
      .pipe(takeUntil(this.destroy$), skip(1), take(1))
      .subscribe((release) => {
        this.appsService.releaseId.set(release.id);
        this.appsService.refreshData();
      });
  }

  ngOnInit(): void {
    this.appServerResourceType$.pipe(takeUntil(this.destroy$)).subscribe((asResourceType) => {
      this.resourceService.setTypeForResourceGroupList(asResourceType);
    });

    this.error$.pipe(takeUntil(this.destroy$)).subscribe((msg) => {
      msg !== '' ? this.toastService.error(msg) : null;
    });

    this.route.queryParams.pipe(takeUntil(this.destroy$)).subscribe((params) => {
      if (params.filter) {
        this.appsService.filter.set(params.filter);
      }
      if (params.releaseId) {
        this.appsService.releaseId.set(Number(params.releaseId));
      }
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
          this.appServerResourceType$.pipe(takeUntil(this.destroy$)).subscribe((asResourceType) => {
            this.resourceService.setTypeForResourceGroupList(asResourceType);
          });
        },
      });
    this.showLoader.set(false);
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
    this.showLoader.set(false);
  }

  updateFilter(values: { filter: string; releaseId: number }) {
    let update = false;
    if (
      !(this.isFilterEmpty(values.filter) && this.isFilterEmpty(this.appsService.filter())) &&
      values.filter !== this.appsService.filter()
    ) {
      this.appsService.filter.set(values.filter);
      update = true;
    }

    if (values.releaseId > 0 && this.appsService.releaseId() !== values.releaseId) {
      this.appsService.releaseId.set(values.releaseId);
      update = true;
    }
    if (update) {
      this.router.navigate([], {
        relativeTo: this.route,
        queryParams: { filter: this.appsService.filter(), releaseId: this.appsService.releaseId() },
      });
      this.appsService.refreshData();
    }
  }

  private isFilterEmpty(value: string | null | undefined): boolean {
    return value === null || value === undefined || value.trim() === '';
  }

  ngOnDestroy(): void {
    this.destroy$.next(undefined);
  }
}
