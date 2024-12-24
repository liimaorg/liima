import { ComponentFixture, TestBed } from '@angular/core/testing';
import { InputSignal, signal } from '@angular/core';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
<<<<<<< HEAD:AMW_angular/io/src/app/resources/resource-type-edit/resource-type-functions/resource-type-functions-list.component.spec.ts
import { ResourceTypeFunctionsListComponent } from './resource-type-functions-list.component';
import { ResourceType } from '../../../resource/resource-type';

describe('ResourceFunctionsComponent', () => {
  let component: ResourceTypeFunctionsListComponent;
  let fixture: ComponentFixture<ResourceTypeFunctionsListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ResourceTypeFunctionsListComponent],
      providers: [provideHttpClient(withInterceptorsFromDi()), provideHttpClientTesting()],
    }).compileComponents();

    fixture = TestBed.createComponent(ResourceTypeFunctionsListComponent);
=======
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
>>>>>>> b2910898... refactor: clean up component naming:AMW_angular/io/src/app/resources/resource-type-edit/resource-type-functions/resource-type-list-functions.component.spec.ts
    component = fixture.componentInstance;
    component.resourceType = signal<ResourceType>(null) as unknown as InputSignal<ResourceType>;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
