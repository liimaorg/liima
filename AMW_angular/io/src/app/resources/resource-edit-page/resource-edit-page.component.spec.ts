import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { ResourceEditPageComponent } from './resource-edit-page.component';
import { ActivatedRoute } from '@angular/router';
import { of, Subject } from 'rxjs';
import { RouterTestingModule } from '@angular/router/testing';

describe('ResourceEditPageComponent', () => {
  let component: ResourceEditPageComponent;
  let fixture: ComponentFixture<ResourceEditPageComponent>;

  const mockRoute: any = { queryParamMap: of() };
  mockRoute.queryParamMap = new Subject<Map<string, number>>();
  mockRoute.queryParamMap.next({
    id: 42,
  });

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ResourceEditPageComponent, RouterTestingModule.withRoutes([])],
      providers: [
        provideHttpClient(withInterceptorsFromDi()),
        provideHttpClientTesting(),
        { provide: ActivatedRoute, useValue: mockRoute },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(ResourceEditPageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
