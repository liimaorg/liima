import { Component, OnInit } from '@angular/core';
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
  canViewSettings = false
  canViewPermissionsTab = false
  canViewStpTab = false
  canViewAppInfo = false

  constructor(
    private authService: AuthService
  ) { }

  ngOnInit(): void {
    this.getUserPermissions();
  }

  private getUserPermissions() {
    this.authService.hasPermission('SETTING_PANEL_LIST', 'ALL').subscribe(value => this.canViewSettings = value)
    this.authService.hasPermission('ROLES_AND_PERMISSIONS_TAB', 'ALL').subscribe(value => this.canViewPermissionsTab = value)
    this.authService.hasPermission('SHAKEDOWNTEST', 'ALL').subscribe(value => this.canViewStpTab = value)
    this.authService.hasPermission('RELEASE', 'READ').subscribe(value => this.canViewAppInfo = value)
  }
}
