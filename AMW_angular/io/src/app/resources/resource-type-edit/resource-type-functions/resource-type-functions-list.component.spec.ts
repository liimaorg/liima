import { ComponentFixture, TestBed } from '@angular/core/testing';
import { InputSignal, signal } from '@angular/core';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { ResourceTypeFunctionsListComponent } from './resource-type-functions-list.component';
import { ResourceType } from '../../models/resource-type';

describe('ResourceFunctionsComponent', () => {
  let component: ResourceTypeFunctionsListComponent;
  let fixture: ComponentFixture<ResourceTypeFunctionsListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ResourceTypeFunctionsListComponent],
      providers: [provideHttpClient(withInterceptorsFromDi()), provideHttpClientTesting()],
    }).compileComponents();

    fixture = TestBed.createComponent(ResourceTypeFunctionsListComponent);
    component = fixture.componentInstance;
    component.resourceType = signal<ResourceType>(null) as unknown as InputSignal<ResourceType>;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
