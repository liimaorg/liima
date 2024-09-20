import { Component, computed, inject, signal } from '@angular/core';
import { BehaviorSubject, Observable, Subject } from 'rxjs';
import { LoadingIndicatorComponent } from '../shared/elements/loading-indicator.component';
import { AsyncPipe } from '@angular/common';
import { IconComponent } from '../shared/icon/icon.component';
import { PageComponent } from '../layout/page/page.component';
import { AppsFilterComponent } from './apps-filter/apps-filter.component';
import { AuthService } from '../auth/auth.service';
import { ReleasesService } from '../settings/releases/releases.service';
import { AppsService } from './apps.service';
import { Release } from '../settings/releases/release';
import { map, takeUntil } from 'rxjs/operators';
import { ToastService } from '../shared/elements/toast/toast.service';
import { AppServer } from './app-server';
import { AppServersListComponent } from './app-servers-list/app-servers-list.component';
import { PaginationComponent } from '../shared/pagination/pagination.component';

@Component({
  selector: 'amw-apps',
  standalone: true,
  imports: [
    AppsFilterComponent,
    AppServersListComponent,
    AsyncPipe,
    IconComponent,
    LoadingIndicatorComponent,
    PageComponent,
    PaginationComponent,
  ],
  templateUrl: './apps.component.html',
  styleUrl: './apps.component.scss',
})
export class AppsComponent {
  appsService = inject(AppsService);
  authService = inject(AuthService);
  releaseService = inject(ReleasesService); // getCount -> getReleases(0, count)
  toastService = inject(ToastService);

  releases$: Observable<Release[]>;
  appServers$: Observable<AppServer[]>;
  private error$ = new BehaviorSubject<string>('');
  private destroy$ = new Subject<void>();

  canCreateApp = false;
  canCreateAppServer = false;
  canViewAppList = false;
  isLoading = false;

  // pagination with default values
  maxResults = 20;
  offset = 0;
  allResults: number;
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
    this.releases$ = this.releaseService.getReleases(null, null);
    this.getAppServers();
    this.isLoading = false;
  }

  private getAppServers() {
    this.currentPage = Math.floor(this.offset / this.maxResults) + 1;
    this.appServers$ = this.appsService
      .getApps(this.offset, this.maxResults, null, 50)
      .pipe(
        map((response) => {
          this.allResults = Number(response.headers.get('x-total-count'));
          this.lastPage = Math.ceil(this.allResults / this.maxResults);
          return response.body;
        }),
      )
      .pipe();
  }

  private getUserPermissions() {
    this.canCreateApp = this.authService.hasResourcePermission('RESOURCE', 'CREATE', 'APPLICATION');
    this.canCreateAppServer = this.authService.hasResourcePermission('RESOURCE', 'CREATE', 'APPLICATIONSERVER');
    this.canViewAppList = this.authService.hasPermission('APP_AND_APPSERVER_LIST', 'READ');
  }

  addApp() {}
  addServer() {}

  setMaxResultsPerPage(max: number) {
    this.maxResults = max;
    this.offset = 0;
    this.getAppServers();
  }

  setNewOffset(offset: number) {
    this.offset = offset;
    this.getAppServers();
  }
}
