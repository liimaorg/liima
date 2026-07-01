import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { ResourceTemplatesListComponent } from './resource-templates-list.component';

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
    fixture.componentRef.setInput('resource', null);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
