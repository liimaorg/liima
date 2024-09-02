import { Component, inject, OnInit, signal } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from '../auth/auth.service';
import { PageComponent } from '../layout/page/page.component';

@Component({
  selector: 'app-settings',
  templateUrl: './settings.component.html',
  styleUrl: './settings.component.scss',
  standalone: true,
  imports: [RouterLink, RouterLinkActive, RouterOutlet, PageComponent],
})
export class SettingsComponent implements OnInit {
  canViewSettings = signal<boolean>(false);
  canViewPermissionsTab = signal<boolean>(false);
  canViewStpTab = signal<boolean>(false);
  canViewAppInfo = signal<boolean>(false);

  authService = inject(AuthService);

  ngOnInit(): void {
    this.getUserPermissions();
  }

  private getUserPermissions() {
    this.canViewSettings.set(this.authService.hasPermission('SETTING_PANEL_LIST', 'ALL'));
    this.canViewPermissionsTab.set(this.authService.hasPermission('ROLES_AND_PERMISSIONS_TAB', 'ALL'));
    this.canViewStpTab.set(this.authService.hasPermission('SHAKEDOWNTEST', 'ALL'));
    this.canViewAppInfo.set(this.authService.hasPermission('RELEASE', 'READ'));
  }
}
