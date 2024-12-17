import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ComponentRef, InputSignal, signal } from '@angular/core';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { ResourceEditFunctionsComponent } from './resource-edit-functions.component';

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
    component.resourceId = signal<number>(0) as unknown as InputSignal<number>;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
