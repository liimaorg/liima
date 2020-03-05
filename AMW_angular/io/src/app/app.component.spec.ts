import { ChangeDetectorRef } from '@angular/core';
import { ComponentFixture, inject, TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { of } from 'rxjs';
import { AppComponent } from './app.component';
import { AppService } from './app.service';
import { AppConfiguration } from './setting/app-configuration';
import { SettingService } from './setting/setting.service';
import { HttpClientTestingModule } from '@angular/common/http/testing';

class RouterStub {
  navigateByUrl(url: string) {
    return url;
  }
}

describe('App', () => {
  let appService: AppService;
  let router: Router;
  let app: AppComponent;
  let fixture: ComponentFixture<AppComponent>;
  let settingService: SettingService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [AppComponent],
      imports: [RouterTestingModule, HttpClientTestingModule],
      providers: [
        AppService,
        SettingService,
        ChangeDetectorRef,
        AppComponent,
        { provide: Router, useClass: RouterStub }
      ]
    }).compileComponents();
    appService = TestBed.inject(AppService);
    router = TestBed.inject(Router);
    settingService = TestBed.inject(SettingService);

    fixture = TestBed.createComponent(AppComponent);
    app = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should have a name', inject([AppComponent], (app: AppComponent) => {
    expect(app.name).toEqual('Angular 8');
  }));

  it('should navigate to the right target', () => {
    // given
    const item: any = { title: 'test', target: 'target' };
    spyOn(appService, 'set').and.callThrough();
    spyOn(router, 'navigateByUrl').and.callThrough();
    // when
    app.navigateTo(item);
    // then
    expect(appService.set).toHaveBeenCalledWith('navTitle', 'test');
    expect(router.navigateByUrl).toHaveBeenCalledWith('target');
  });

  it('should set logoutUrl on ngOnInit', () => {
    // given
    const expectedKey: string = 'logoutUrl';
    const expectedValue: string = 'testUrl';
    const configKeyVal: string = 'amw.logoutUrl';
    const configKeyEnv: string = 'AMW_LOGOUTURL';
    const appConf: AppConfiguration = {
      key: { value: configKeyVal, env: configKeyEnv },
      value: expectedValue
    } as AppConfiguration;
    spyOn(settingService, 'getAllAppSettings').and.returnValues(of([appConf]));
    spyOn(appService, 'set').and.callThrough();
    // when
    app.ngOnInit();
    // then
    expect(appService.set).toHaveBeenCalledWith(expectedKey, expectedValue);
  });

  it('should set empty logoutUrl on ngOnInit if config not found', () => {
    // given
    const expectedKey: string = 'logoutUrl';
    const expectedValue: string = '';
    const appConf: AppConfiguration = {
      key: { value: 'test', env: 'TEST' }
    } as AppConfiguration;
    spyOn(settingService, 'getAllAppSettings').and.returnValues(of([appConf]));
    spyOn(appService, 'set').and.callThrough();
    // when
    app.ngOnInit();
    // then
    expect(appService.set).toHaveBeenCalledWith(expectedKey, expectedValue);
  });
});
