import { ComponentFixture, TestBed } from '@angular/core/testing';
import { InputSignal, signal } from '@angular/core';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { ResourceTypeTemplatesListComponent } from './resource-type-templates-list.component';
import { ResourceType } from '../../models/resource-type';

describe('ResourceTypeTemplatesListComponent', () => {
  let component: ResourceTypeTemplatesListComponent;
  let fixture: ComponentFixture<ResourceTypeTemplatesListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ResourceTypeTemplatesListComponent],
      providers: [provideHttpClient(withInterceptorsFromDi()), provideHttpClientTesting()],
    }).compileComponents();

    fixture = TestBed.createComponent(ResourceTypeTemplatesListComponent);
    component = fixture.componentInstance;
    component.resourceType = signal<ResourceType>(null) as unknown as InputSignal<ResourceType>;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
