import { inject, TestBed } from '@angular/core/testing';
import { BaseService } from './base.service';
import { HttpHeaders } from '@angular/common/http';

let baseService: BaseService;

describe('BaseService', () => {
  beforeEach(() => {
    baseService = new BaseService();
  });

  it('should return base url', () => {
    const expectedValue: string = '/AMW_rest/resources';
    const url: string = baseService.getBaseUrl();
    expect(url).toBe(expectedValue);
  });

  it('should return default headers for get requests', () => {
    let headers: HttpHeaders = baseService.getHeaders();
    expect(headers.get('Accept')).toBe('application/json');
  });

  it('should return default headers for post requests', () => {
    let headers: HttpHeaders = baseService.postHeaders();
    expect(headers.get('Content-Type')).toBe('application/json');
    expect(headers.get('Accept')).toBe('application/json');
  });

  it('should have method to handle errors', () => {
    expect(baseService.handleError).toBeDefined();
  });
});
