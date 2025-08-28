import { TestBed } from '@angular/core/testing';
import { SettingService } from './setting.service';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';

describe('SettingService', () => {
  let httpTestingController: HttpTestingController;
  let settingService: SettingService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [],
      providers: [SettingService, provideHttpClient(withInterceptorsFromDi()), provideHttpClientTesting()],
    });

    httpTestingController = TestBed.inject(HttpTestingController);
    settingService = TestBed.inject(SettingService);
  });

  afterEach(() => {
    httpTestingController.verify();
  });

  it('should have a getAllAppSettings method', () => {
    expect(settingService.getAllAppSettings()).toBeDefined();
  });

  it('should request data from the right endpoint when getAllAppSettings is called', () => {
    const settingsResponse = {
      key: {
        value: 'amw.logsPath',
        env: 'AMW_LOGSPATH',
      },
      value: '/tmp/amw/logs',
      defaultValue: null,
    };

    settingService.getAllAppSettings().subscribe((settingRes) => {
      expect(settingRes).toEqual([settingsResponse]);
    });

    const req = httpTestingController.expectOne('/AMW_rest/resources/settings');

    expect(req.request.method).toEqual('GET');

    req.flush([settingsResponse]);
  });
});
