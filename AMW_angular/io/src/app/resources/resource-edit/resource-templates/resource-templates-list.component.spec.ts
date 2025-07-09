import { ComponentFixture, TestBed } from '@angular/core/testing';
import { InputSignal, signal } from '@angular/core';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { ResourceTemplatesListComponent } from './resource-templates-list.component';
import { Resource } from '../../../resource/resource';

describe('ResourceTemplatesComponent', () => {
  let component: ResourceTemplatesListComponent;
  let fixture: ComponentFixture<ResourceTemplatesListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ResourceTemplatesListComponent],
      providers: [provideHttpClient(withInterceptorsFromDi()), provideHttpClientTesting()],
    }).compileComponents();

    fixture = TestBed.createComponent(ResourceTemplatesListComponent);
    component = fixture.componentInstance;
    component.resource = signal<Resource>(null) as unknown as InputSignal<Resource>;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
