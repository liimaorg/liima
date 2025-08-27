import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { ResourceTypeEditComponent } from './resource-type-edit.component';
import { ActivatedRoute } from '@angular/router';
import { of, Subject } from 'rxjs';

describe('ResourceEditPageComponent', () => {
  let component: ResourceTypeEditComponent;
  let fixture: ComponentFixture<ResourceTypeEditComponent>;

  const mockRoute: any = { queryParamMap: of() };
  mockRoute.queryParamMap = new Subject<Map<string, number>>();
  mockRoute.queryParamMap.next({
    id: 42,
  });

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ResourceTypeEditComponent],
      providers: [
        provideHttpClient(withInterceptorsFromDi()),
        provideHttpClientTesting(),
        { provide: ActivatedRoute, useValue: mockRoute },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(ResourceTypeEditComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
