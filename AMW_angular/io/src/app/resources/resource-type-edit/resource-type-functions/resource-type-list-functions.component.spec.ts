import { ComponentFixture, TestBed } from '@angular/core/testing';
import { InputSignal, signal } from '@angular/core';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { ResourceTypeListFunctionsComponent } from './resource-type-list-functions.component';
import { ResourceType } from '../../../resource/resource-type';

describe('ResourceFunctionsComponent', () => {
  let component: ResourceTypeListFunctionsComponent;
  let fixture: ComponentFixture<ResourceTypeListFunctionsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ResourceTypeListFunctionsComponent],
      providers: [provideHttpClient(withInterceptorsFromDi()), provideHttpClientTesting()],
    }).compileComponents();

    fixture = TestBed.createComponent(ResourceTypeListFunctionsComponent);
    component = fixture.componentInstance;
    component.resourceType = signal<ResourceType>(null) as unknown as InputSignal<ResourceType>;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
