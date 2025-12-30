import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { signal } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { RouterModule } from '@angular/router';
import { ENVIRONMENT } from '../core/amw-constants';
import { ServersPageComponent } from './servers-page.component';
import { ConfigurationService } from '../shared/service/configuration.service';

describe(ServersPageComponent.name, () => {
  let component: ServersPageComponent;
  let fixture: ComponentFixture<ServersPageComponent>;
  let mockConfigurationSignal: ReturnType<typeof signal>;

  beforeEach(async () => {
    mockConfigurationSignal = signal([]);

    await TestBed.configureTestingModule({
      imports: [ServersPageComponent, RouterModule.forRoot([], { useHash: true })],
      providers: [
        provideHttpClient(withInterceptorsFromDi()),
        provideHttpClientTesting(),
        { provide: ConfigurationService, useValue: { configuration: mockConfigurationSignal } },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(ServersPageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
    expect(component.isLoading).toBe(false);
  });

  describe('linkToHostUrl', () => {
    it('should return URL with query param if both AMW_VM_DETAILS_URL and AMW_VM_URL_PARAM are present', () => {
      mockConfigurationSignal.set([
        { key: { value: ENVIRONMENT.AMW_VM_DETAILS_URL }, value: 'http://details.url/vm' },
        { key: { value: ENVIRONMENT.AMW_VM_URL_PARAM }, value: 'host' },
      ]);
      fixture.detectChanges();
      expect(component.linkToHostUrl()).toBe('http://details.url/vm?host={hostName}');
    });

    it('should return URL without query param if only AMW_VM_DETAILS_URL is present', () => {
      mockConfigurationSignal.set([
        { key: { value: ENVIRONMENT.AMW_VM_DETAILS_URL }, value: 'http://details.url/vm?server={hostName}' },
      ]);
      fixture.detectChanges();
      expect(component.linkToHostUrl()).toBe('http://details.url/vm?server={hostName}');
    });

    it('should return undefined if configuration is empty', () => {
      mockConfigurationSignal.set([]);
      fixture.detectChanges();
      expect(component.linkToHostUrl()).toBeUndefined();
    });

    it('should return undefined if configuration is undefined', () => {
      mockConfigurationSignal.set(undefined);
      fixture.detectChanges();
      expect(component.linkToHostUrl()).toBeUndefined();
    });
  });
});
