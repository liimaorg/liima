import { ComponentFixture, TestBed } from '@angular/core/testing';
import { InputSignal, signal } from '@angular/core';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
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
    component = fixture.componentInstance;
    component.resource = signal<Resource>(null) as unknown as InputSignal<Resource>;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
