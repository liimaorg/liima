import { Component, computed, inject, signal } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { LoadingIndicatorComponent } from '../shared/elements/loading-indicator.component';
import { AsyncPipe } from '@angular/common';
import { IconComponent } from '../shared/icon/icon.component';
import { PageComponent } from '../layout/page/page.component';
import { AppsListComponent } from './apps-list/apps-list.component';
import { AppsFilterComponent } from './apps-filter/apps-filter.component';
import { AuthService } from '../auth/auth.service';
import { ReleasesService } from '../settings/releases/releases.service';
import { AppsService } from './apps.service';

@Component({
  selector: 'amw-apps',
  standalone: true,
  imports: [AppsFilterComponent, AppsListComponent, AsyncPipe, IconComponent, LoadingIndicatorComponent, PageComponent],
  templateUrl: './apps.component.html',
  styleUrl: './apps.component.scss',
})
export class AppsComponent {
  appsService = inject(AppsService);
  authService = inject(AuthService);
  releaseService = inject(ReleasesService); // getCount -> getReleases(0, count)
  canCreateApp = false;
  canCreateAppServer = false;
  canViewAppList = false;
  isLoading = false;

  loadingPermissions = computed(() => {
    if (this.authService.restrictions().length > 0) {
      console.log(this.authService.restrictions());
      this.getUserPermissions();
    } else {
      return `<div>Could not load permissions</div>`;
    }
  });

  ngOnInit(): void {
    this.isLoading = false;
    this.appsService
      .getApps(0, 100)
      .pipe()
      .subscribe((result) => console.log(result));
  }

  private getUserPermissions() {
    this.canCreateApp = this.authService.hasResourcePermission('RESOURCE', 'CREATE', 'APPLICATION');
    this.canCreateAppServer = this.authService.hasResourcePermission('RESOURCE', 'CREATE', 'APPLICATIONSERVER');
    this.canViewAppList = this.authService.hasPermission('APP_AND_APPSERVER_LIST', 'READ');
  }

  addApp() {}
  addServer() {}
}
