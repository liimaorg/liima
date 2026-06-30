import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { ResourceTypeFunctionsListComponent } from './resource-type-functions-list.component';

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
    fixture.componentRef.setInput('resourceType', null);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
