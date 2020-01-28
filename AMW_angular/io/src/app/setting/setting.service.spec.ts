import {TestBed} from '@angular/core/testing';
import {SettingService} from './setting.service';
import {HttpClientTestingModule, HttpTestingController} from '@angular/common/http/testing';
import {HttpClient} from "@angular/common/http";

describe('SettingService', () => {

  let httpClient: HttpClient;
  let httpTestingController: HttpTestingController;
  let settingService: SettingService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [SettingService]
    });

    httpTestingController = TestBed.get(HttpTestingController);
    httpClient = TestBed.get(HttpClient);
    settingService = TestBed.get(SettingService);
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
        env: 'AMW_LOGSPATH'
      },
      value: '/tmp/amw/logs',
      defaultValue: null
    };

    settingService.getAllAppSettings().subscribe(settingRes => {
      expect(settingRes).toEqual([settingsResponse])
    });

    const req = httpTestingController.expectOne(
      '/AMW_rest/resources/settings'
    );

    expect(req.request.method).toEqual('GET');

    req.flush([settingsResponse]);
  });

});
