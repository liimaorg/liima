import { ComponentFixture, TestBed } from '@angular/core/testing';
import { InputSignal, signal } from '@angular/core';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
<<<<<<< HEAD:AMW_angular/io/src/app/resources/resource-edit/resource-functions/resource-functions-list.component.spec.ts
import { ResourceFunctionsListComponent } from './resource-functions-list.component';
import { Resource } from '../../../resource/resource';

describe('ResourceFunctionsComponent', () => {
  let component: ResourceFunctionsListComponent;
  let fixture: ComponentFixture<ResourceFunctionsListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ResourceFunctionsListComponent],
      providers: [provideHttpClient(withInterceptorsFromDi()), provideHttpClientTesting()],
    }).compileComponents();

    fixture = TestBed.createComponent(ResourceFunctionsListComponent);
=======
import { ResourceListFunctionsComponent } from './resource-list-functions.component';
import { Resource } from '../../../resource/resource';

describe('ResourceFunctionsComponent', () => {
  let component: ResourceListFunctionsComponent;
  let fixture: ComponentFixture<ResourceListFunctionsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ResourceListFunctionsComponent],
      providers: [provideHttpClient(withInterceptorsFromDi()), provideHttpClientTesting()],
    }).compileComponents();

    fixture = TestBed.createComponent(ResourceListFunctionsComponent);
>>>>>>> b2910898... refactor: clean up component naming:AMW_angular/io/src/app/resources/resource-edit/resource-functions/resource-list-functions.component.spec.ts
    component = fixture.componentInstance;
    component.resource = signal<Resource>(null) as unknown as InputSignal<Resource>;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
