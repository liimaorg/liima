import { inject, TestBed } from '@angular/core/testing';
import { BaseService } from './base.service';
import { AppConfiguration } from './app-configuration';

describe('BaseService', () => {
  beforeEach(() => TestBed.configureTestingModule({
    providers: [BaseService]
  }));

  it('should have an extractPayload method',
    inject([BaseService], (baseService: BaseService) => {
      expect(baseService.extractPayload).toBeDefined();
  }));

  it('should have a getBaseUrl method',
    inject([BaseService], (baseService: BaseService) => {
      expect(baseService.getBaseUrl).toBeDefined();
  }));

  it('should return the expected value on getBaseUrl method',
    inject([BaseService], (baseService: BaseService) => {
      const expectedValue: string = '/AMW_rest/resources';
      const url: string = baseService.getBaseUrl();
      expect(url).toBe(expectedValue);
  }));

  it('should have a getHeaders method',
    inject([BaseService], (baseService: BaseService) => {
      expect(baseService.getHeaders).toBeDefined();
  }));

  it('should have a postHeaders method',
    inject([BaseService], (baseService: BaseService) => {
      expect(baseService.postHeaders).toBeDefined();
  }));

});
