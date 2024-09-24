import { Component, computed, inject } from '@angular/core';
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
export class SettingsComponent {
  authService = inject(AuthService);
  canViewSettings = false;
  canViewPermissionsTab = false;
  canViewAppInfo = false;

  loadingPermissions = computed(() => {
    if (this.authService.restrictions().length > 0) {
      this.getUserPermissions();
    } else {
      return `<div>Could not load permissions</div>`;
    }
  });

  private getUserPermissions() {
    this.canViewSettings = this.authService.hasPermission('SETTING_PANEL_LIST', 'ALL');
    this.canViewPermissionsTab = this.authService.hasPermission('ROLES_AND_PERMISSIONS_TAB', 'ALL');
    this.canViewAppInfo = this.authService.hasPermission('RELEASE', 'READ');
  }
}
