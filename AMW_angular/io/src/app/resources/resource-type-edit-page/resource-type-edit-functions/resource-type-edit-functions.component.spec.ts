import { ComponentFixture, TestBed } from '@angular/core/testing';
import { InputSignal, signal } from '@angular/core';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { ResourceTypeEditFunctionsComponent } from './resource-type-edit-functions.component';
import { Resource } from '../../../resource/resource';
import { ResourceType } from '../../../resource/resource-type';

describe('ResourceFunctionsComponent', () => {
  let component: ResourceTypeEditFunctionsComponent;
  let fixture: ComponentFixture<ResourceTypeEditFunctionsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ResourceTypeEditFunctionsComponent],
      providers: [provideHttpClient(withInterceptorsFromDi()), provideHttpClientTesting()],
    }).compileComponents();

    fixture = TestBed.createComponent(ResourceTypeEditFunctionsComponent);
    component = fixture.componentInstance;
    component.resourceType = signal<ResourceType>(null) as unknown as InputSignal<ResourceType>;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
