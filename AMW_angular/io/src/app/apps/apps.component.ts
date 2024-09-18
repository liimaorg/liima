import { Component, inject, signal } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { LoadingIndicatorComponent } from '../shared/elements/loading-indicator.component';
import { AsyncPipe } from '@angular/common';
import { IconComponent } from '../shared/icon/icon.component';
import { PageComponent } from '../layout/page/page.component';
import { AppsListComponent } from './apps-list/apps-list.component';
import { AppsFilterComponent } from './apps-filter/apps-filter.component';
import { AuthService } from '../auth/auth.service';

@Component({
  selector: 'amw-apps',
  standalone: true,
  imports: [AppsFilterComponent, AppsListComponent, AsyncPipe, IconComponent, LoadingIndicatorComponent, PageComponent],
  templateUrl: './apps.component.html',
  styleUrl: './apps.component.scss',
})
export class AppsComponent {
  authService = inject(AuthService);
  isLoading$: Observable<boolean> = new BehaviorSubject<boolean>(true);
  canCreateApp = signal<boolean>(false);
  canCreateAppServer = signal<boolean>(false);
  canViewAppList = signal<boolean>(false);

  ngOnInit(): void {
    this.getUserPermissions();
  }

  private getUserPermissions() {
    this.canCreateApp.set(this.authService.hasPermission('APPLICATION', 'CREATE'));
    this.canCreateAppServer.set(this.authService.hasPermission('APPLICATIONSERVER', 'CREATE'));
    this.canViewAppList.set(this.authService.hasPermission('APP_AND_APPSERVER_LIST', 'READ'));
  }

  addApp() {}
  addServer() {}
}
