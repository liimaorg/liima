import { ComponentFixture, TestBed } from '@angular/core/testing';
import { InputSignal, signal } from '@angular/core';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { ResourceEditFunctionsComponent } from './resource-edit-functions.component';
import { Resource } from '../../../resource/resource';

describe('ResourceFunctionsComponent', () => {
  let component: ResourceEditFunctionsComponent;
  let fixture: ComponentFixture<ResourceEditFunctionsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ResourceEditFunctionsComponent],
      providers: [provideHttpClient(withInterceptorsFromDi()), provideHttpClientTesting()],
    }).compileComponents();

    fixture = TestBed.createComponent(ResourceEditFunctionsComponent);
    component = fixture.componentInstance;
    component.resource = signal<Resource>(null) as unknown as InputSignal<Resource>;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
