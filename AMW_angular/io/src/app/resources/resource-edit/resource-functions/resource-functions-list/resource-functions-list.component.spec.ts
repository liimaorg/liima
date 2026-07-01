import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { ResourceFunctionsListComponent } from './resource-functions-list.component';

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
    fixture.componentRef.setInput('resource', null);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
