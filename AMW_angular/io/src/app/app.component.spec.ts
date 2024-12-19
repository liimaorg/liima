import { ChangeDetectorRef } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter, Router, withHashLocation } from '@angular/router';
import { of } from 'rxjs';
import { AppComponent } from './app.component';
import { NavigationComponent } from './navigation/navigation.component';

import { AppConfiguration } from './setting/app-configuration';
import { SettingService } from './setting/setting.service';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { routes } from './app.routes';

class RouterStub {
  navigateByUrl(url: string) {
    return url;
  }
}

describe('App', () => {
  let app: AppComponent;
  let fixture: ComponentFixture<AppComponent>;
  let settingService: SettingService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [NavigationComponent, AppComponent],
      providers: [
        SettingService,
        ChangeDetectorRef,
        AppComponent,
        { provide: Router, useClass: RouterStub },
        provideHttpClient(withInterceptorsFromDi()),
        provideHttpClientTesting(),
        provideRouter(routes, withHashLocation()),
      ],
    }).compileComponents();
    settingService = TestBed.inject(SettingService);

    fixture = TestBed.createComponent(AppComponent);
    app = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should set logoutUrl on ngOnInit', () => {
    // given
    const expectedValue: string = 'testUrl';
    const configKeyVal: string = 'amw.logoutUrl';
    const configKeyEnv: string = 'AMW_LOGOUTURL';
    const appConf: AppConfiguration = {
      key: { value: configKeyVal, env: configKeyEnv },
      value: expectedValue,
    } as AppConfiguration;
    spyOn(settingService, 'getAllAppSettings').and.returnValues(of([appConf]));

    app.ngOnInit();

    expect(app.logoutUrl).toEqual(expectedValue);
  });

  it('should set empty logoutUrl if config not found', () => {
    // given
    const expectedValue: string = '';
    const appConf: AppConfiguration = {
      key: { value: 'test', env: 'TEST' },
    } as AppConfiguration;
    spyOn(settingService, 'getAllAppSettings').and.returnValues(of([appConf]));

    app.ngOnInit();

    expect(app.logoutUrl).toEqual(expectedValue);
  });
});
