import { inject, TestBed } from '@angular/core/testing';
import { SettingService } from './setting.service';
import { AppConfiguration } from './app-configuration';
import { HttpTestingController } from '@angular/common/http/testing';

describe('SettingService', () => {
  beforeEach(() =>
    TestBed.configureTestingModule({
      imports: [HttpTestingController],
      providers: [SettingService]
    })
  );

  it('should have a getAllAppSettings method', inject(
    [SettingService],
    (settingService: SettingService) => {
      expect(settingService.getAllAppSettings()).toBeDefined();
    }
  ));

  // it('should request data from the right endpoint when getAllAppSettings is called', inject(
  //   // TODO
  //   [SettingService, MockBackend],
  //   (settingService: SettingService, mockBackend: typeof MockBackend) => {
  //     // given
  //     mockBackend.connections.subscribe(connection => {
  //       expect(connection.request.method).toBe(RequestMethod.Get);
  //       expect(connection.request.url).toMatch('/AMW_rest/resources/settings');
  //       const mockResponse = new Response(
  //         new ResponseOptions({ body: [{} as AppConfiguration] })
  //       );
  //       connection.mockRespond(mockResponse);
  //     });
  //     // when then
  //     settingService.getAllAppSettings().subscribe(response => {
  //       // TODO
  //       //expect(response).toEqual([{}]);
  //     });
  //   }
  // ));

  // it('should handle backend errors', inject(
  //   // TODO
  //   [SettingService, MockBackend],
  //   (settingService: SettingService, mockBackend: typeof MockBackend) => {
  //     // given
  //     mockBackend.connections.subscribe(connection => {
  //       expect(connection.request.method).toBe(RequestMethod.Get);
  //       expect(connection.request.url).toMatch('/AMW_rest/resources/settings');
  //       connection.mockError({});
  //     });
  //     // when then
  //     expect(function() {
  //       settingService.getAllAppSettings().subscribe();
  //     }).toThrow('Error retrieving your data');
  //   }
  // ));
});
