import { ChangeDetectorRef } from '@angular/core';
import { inject, TestBed } from '@angular/core/testing';
import { ConnectionBackend, Http, HttpModule } from '@angular/http';
import { Router } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { Observable } from 'rxjs/Rx';

// Load the implementations that should be tested
import { AppComponent } from './app.component';
import { AppState } from './app.service';
import { SettingService } from './setting/setting.service';
import { AppConfiguration } from './setting/app-configuration';

class RouterStub {
  navigateByUrl( url: string ) { return url; }
}

describe('App', () => {
  // provide our implementations or mocks to the dependency injector
  beforeEach(() => TestBed.configureTestingModule({
    imports: [ HttpModule, RouterTestingModule ],
    providers: [
      AppState,
      AppComponent,
      SettingService,
      ChangeDetectorRef,
      Http,
      ConnectionBackend,
      { provide: Router, useClass: RouterStub }
    ]
  }));

  it('should have a name', inject([AppComponent], (app: AppComponent) => {
    expect(app.name).toEqual('Angular 4');
  }));

  it('should navigate to the right target',
    inject([AppComponent, AppState, Router], (app: AppComponent, appState: AppState, router: Router) => {
    // given
    const item: any = {title: 'test', target: 'target'};
    spyOn(appState, 'set').and.callThrough();
    spyOn(router, 'navigateByUrl').and.callThrough();
    // when
    app.navigateTo(item);
    // then
    expect(appState.set).toHaveBeenCalledWith('navTitle', 'test');
    expect(router.navigateByUrl).toHaveBeenCalledWith('target');
  }));

  it('should set logoutUrl on ngOnInit',
    inject( [AppComponent, AppState, SettingService ], (app: AppComponent, appState: AppState, settingService: SettingService) => {
      // given
      const expectedKey: string = 'logoutUrl';
      const expectedValue: string = 'testUrl';
      const configKeyVal: string = 'amw.logoutUrl';
      const configKeyEnv: string = 'AMW_LOGOUTURL';
      const appConf: AppConfiguration = {key: { value: configKeyVal, env: configKeyEnv }, value: expectedValue } as AppConfiguration;
      spyOn(settingService, 'getAllAppSettings').and.returnValues(Observable.of([ appConf ]));
      spyOn(appState, 'set').and.callThrough();
      // when
      app.ngOnInit();
      // then
      expect(appState.set).toHaveBeenCalledWith(expectedKey, expectedValue);
  }));

  it('should set empty logoutUrl on ngOnInit if config not found',
    inject( [AppComponent, AppState, SettingService ], (app: AppComponent, appState: AppState, settingService: SettingService) => {
      // given
      const expectedKey: string = 'logoutUrl';
      const expectedValue: string = '';
      const appConf: AppConfiguration =  {key: { value: 'test', env: 'TEST'}} as AppConfiguration;
      spyOn(settingService, 'getAllAppSettings').and.returnValues(Observable.of([ appConf ]));
      spyOn(appState, 'set').and.callThrough();
      // when
      app.ngOnInit();
      // then
      expect(appState.set).toHaveBeenCalledWith(expectedKey, expectedValue);
  }));

});
