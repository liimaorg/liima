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
import { takeUntil } from 'rxjs/operators';
import { ToastService } from '../shared/elements/toast/toast.service';
import { AppServer } from './app-server';
import { AppServersListComponent } from './app-servers-list/app-servers-list.component';

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
    this.appServers$ = this.appsService.getApps(0, 100, null, 50);
    this.isLoading = false;
  }

  private getUserPermissions() {
    this.canCreateApp = this.authService.hasResourcePermission('RESOURCE', 'CREATE', 'APPLICATION');
    this.canCreateAppServer = this.authService.hasResourcePermission('RESOURCE', 'CREATE', 'APPLICATIONSERVER');
    this.canViewAppList = this.authService.hasPermission('APP_AND_APPSERVER_LIST', 'READ');
  }

  addApp() {}
  addServer() {}
}
