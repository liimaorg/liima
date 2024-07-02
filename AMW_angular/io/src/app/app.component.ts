import { Component, ViewEncapsulation, OnInit } from '@angular/core';
import { Router, RouterOutlet } from '@angular/router';
import { SettingService } from './setting/setting.service';
import { AppConfiguration } from './setting/app-configuration';
import { AMW_LOGOUT_URL } from './core/amw-constants';
import { AsyncPipe } from '@angular/common';
import { NavigationComponent } from './navigation/navigation.component';
import { ToastsContainerComponent } from './shared/elements/toast/toasts-container.component';

@Component({
  selector: 'app',
  encapsulation: ViewEncapsulation.None,
  styleUrls: ['./app.component.scss'],
  templateUrl: './app.component.html',
  standalone: true,
  imports: [RouterOutlet, AsyncPipe, NavigationComponent, ToastsContainerComponent],
})
export class AppComponent implements OnInit {
  logoutUrl: string;

  constructor(
    private router: Router,
    private settingService: SettingService,
  ) {}

  ngOnInit(): void {
    this.settingService.getAllAppSettings().subscribe((r) => this.configureSettings(r));
  }

  private configureSettings(settings: AppConfiguration[]) {
    const logoutUrl = settings.find((config) => config.key.value === AMW_LOGOUT_URL);
    this.logoutUrl = logoutUrl ? logoutUrl.value : '';
  }
}
