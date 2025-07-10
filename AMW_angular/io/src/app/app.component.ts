import { Component, ViewEncapsulation, OnInit } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { SettingService } from './setting/setting.service';
import { AppConfiguration } from './setting/app-configuration';
import { AMW_LOGOUT_URL } from './core/amw-constants';
import { AsyncPipe } from '@angular/common';
import { NavigationComponent } from './navigation/navigation.component';
import { ToastContainerComponent } from './shared/elements/toast/toast-container.component';

@Component({
  selector: 'app',
  encapsulation: ViewEncapsulation.None,
  styleUrls: ['./app.component.scss'],
  templateUrl: './app.component.html',
  imports: [RouterOutlet, NavigationComponent, ToastContainerComponent],
})
export class AppComponent implements OnInit {
  logoutUrl: string;

  constructor(private settingService: SettingService) {}

  ngOnInit(): void {
    this.settingService.getAllAppSettings().subscribe((r) => this.configureSettings(r));
  }

  private configureSettings(settings: AppConfiguration[]) {
    const logoutUrl = settings.find((config) => config.key.value === AMW_LOGOUT_URL);
    this.logoutUrl = logoutUrl ? logoutUrl.value : '';
  }
}
