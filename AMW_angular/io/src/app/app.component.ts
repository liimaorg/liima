import { Component, ViewEncapsulation, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AppService, Keys, InternalStateType } from './app.service';
import { SettingService } from './setting/setting.service';
import { AppConfiguration } from './setting/app-configuration';
import { AMW_LOGOUT_URL } from './core/amw-constants';
import { Observable, of } from 'rxjs';
import { map } from 'rxjs/operators';
import { NavigationItem } from './navigation/navigation-item';

@Component({
  selector: 'app',
  encapsulation: ViewEncapsulation.None,
  styleUrls: ['./app.component.scss'],
  templateUrl: './app.component.html'
})
export class AppComponent implements OnInit {
  navigationState$: Observable<InternalStateType>;

  logoutUrl: string;

  constructor(
    public appService: AppService,
    private router: Router,
    private settingService: SettingService
  ) {
    this.navigationState$ = this.appService.state$.pipe(
      map(state => ({
        navigationItems: state[Keys.NavItems],
        current: state[Keys.NavTitle],
        isVisible: state[Keys.NavShow],
        pageTitle: state[Keys.PageTitle]
      }))
    );
  }

  ngOnInit(): void {
    this.settingService
      .getAllAppSettings()
      .subscribe(r => this.configureSettings(r));
  }

  navigateTo(item: NavigationItem): void {
    this.appService.set(Keys.NavTitle, item.title);
    this.router.navigateByUrl(item.target);
  }

  private configureSettings(settings: AppConfiguration[]) {
    const logoutUrl = settings.find(
      config => config.key.value === AMW_LOGOUT_URL
    );
    this.logoutUrl = logoutUrl ? logoutUrl.value : '';
  }
}
