import { ComponentFixture, TestBed } from '@angular/core/testing';
import { InputSignal, signal } from '@angular/core';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
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
    component = fixture.componentInstance;
    component.resource = signal<Resource>(null) as unknown as InputSignal<Resource>;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
