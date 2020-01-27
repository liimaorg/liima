import { ChangeDetectorRef } from '@angular/core';
import { inject, TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { of } from 'rxjs';
// Load the implementations that should be tested
import { AppComponent } from './app.component';
import { AppService } from './app.service';
import { AppConfiguration } from './setting/app-configuration';
import { SettingService } from './setting/setting.service';

class RouterStub {
  navigateByUrl(url: string) {
    return url;
  }
}

describe('App', () => {
  // provide our implementations or mocks to the dependency injector
  beforeEach(() =>
    TestBed.configureTestingModule({
      imports: [RouterTestingModule],
      providers: [
        AppService,
        AppComponent,
        SettingService,
        ChangeDetectorRef,
        { provide: Router, useClass: RouterStub }
      ]
    })
  );

  it('should have a name', inject([AppComponent], (app: AppComponent) => {
    expect(app.name).toEqual('Angular 4');
  }));

  it('should navigate to the right target', inject(
    [AppComponent, AppService, Router],
    (app: AppComponent, appService: AppService, router: Router) => {
      // given
      const item: any = { title: 'test', target: 'target' };
      spyOn(appService, 'set').and.callThrough();
      spyOn(router, 'navigateByUrl').and.callThrough();
      // when
      app.navigateTo(item);
      // then
      expect(appService.set).toHaveBeenCalledWith('navTitle', 'test');
      expect(router.navigateByUrl).toHaveBeenCalledWith('target');
    }
  ));

  it('should set logoutUrl on ngOnInit', inject(
    [AppComponent, AppService, SettingService],
    (app: AppComponent, appService: AppService, settingService: SettingService) => {
      // given
      const expectedKey: string = 'logoutUrl';
      const expectedValue: string = 'testUrl';
      const configKeyVal: string = 'amw.logoutUrl';
      const configKeyEnv: string = 'AMW_LOGOUTURL';
      const appConf: AppConfiguration = {
        key: { value: configKeyVal, env: configKeyEnv },
        value: expectedValue
      } as AppConfiguration;
      spyOn(settingService, 'getAllAppSettings').and.returnValues(
        of([appConf])
      );
      spyOn(appService, 'set').and.callThrough();
      // when
      app.ngOnInit();
      // then
      expect(appService.set).toHaveBeenCalledWith(expectedKey, expectedValue);
    }
  ));

  it('should set empty logoutUrl on ngOnInit if config not found', inject(
    [AppComponent, AppService, SettingService],
    (app: AppComponent, appService: AppService, settingService: SettingService) => {
      // given
      const expectedKey: string = 'logoutUrl';
      const expectedValue: string = '';
      const appConf: AppConfiguration = {
        key: { value: 'test', env: 'TEST' }
      } as AppConfiguration;
      spyOn(settingService, 'getAllAppSettings').and.returnValues(
        of([appConf])
      );
      spyOn(appService, 'set').and.callThrough();
      // when
      app.ngOnInit();
      // then
      expect(appService.set).toHaveBeenCalledWith(expectedKey, expectedValue);
    }
  ));
});
