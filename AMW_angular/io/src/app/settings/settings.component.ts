import { Component, computed, inject } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from '../auth/auth.service';
import { PageComponent } from '../layout/page/page.component';

@Component({
  selector: 'app-settings',
  templateUrl: './settings.component.html',
  imports: [RouterLink, RouterLinkActive, RouterOutlet, PageComponent],
})
export class SettingsComponent {
  authService = inject(AuthService);

  permissions = computed(() => {
    if (this.authService.restrictions().length > 0) {
      return {
        canViewSettings: this.authService.hasPermission('SETTING_PANEL_LIST', 'ALL'),
        canViewPermissionsTab: this.authService.hasPermission('ROLES_AND_PERMISSIONS_TAB', 'ALL'),
        canViewAppInfo: this.authService.hasPermission('RELEASE', 'READ'),
      };
    } else {
      return { canViewSettings: false, canViewPermissionsTab: false, canViewAppInfo: false };
    }
  });
}
