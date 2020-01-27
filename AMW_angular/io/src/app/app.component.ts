/*
 * Angular 4 decorators and services
 */
import { Component, ViewEncapsulation, OnInit, ChangeDetectorRef, AfterViewChecked } from '@angular/core';
import { Router } from '@angular/router';
import { AppService } from './app.service';
import { SettingService } from './setting/setting.service';
import { AppConfiguration } from './setting/app-configuration';
import * as _ from 'lodash';

/*
 * App Component
 * Top Level Component
 */
@Component({
  selector: 'app',
  encapsulation: ViewEncapsulation.None,
  styleUrls: [
    './app.component.scss'
  ],
  templateUrl: './app.component.html'
})
export class AppComponent implements OnInit, AfterViewChecked {
  name = 'Angular 4';

  private logoutUrlKey: string = 'amw.logoutUrl';

  constructor(public appService: AppService,
              private router: Router,
              private cdRef: ChangeDetectorRef,
              private settingService: SettingService) {
  }

  ngOnInit() {
    this.fetchLogoutUrl();
  }

  ngAfterViewChecked() {
    // explicit change detection to avoid "expression-has-changed-after-it-was-checked-error"
    this.cdRef.detectChanges();
  }

  navigateTo(item: any) {
    this.appService.set('navTitle', item.title);
    this.router.navigateByUrl(item.target);
  }

  private fetchLogoutUrl() {
    this.settingService.getAllAppSettings().subscribe(
      /* happy path */ (r) => this.setLogoutUrl(r)
    );
  }

  private setLogoutUrl(settings: AppConfiguration[]) {
    const logoutUrl: AppConfiguration = _.find(settings, ['key.value', this.logoutUrlKey]);
    this.appService.set('logoutUrl', logoutUrl ? logoutUrl.value : '');
  }

}
